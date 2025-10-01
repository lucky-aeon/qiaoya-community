# 登录设备限制技术方案（升级：基于设备 + IP 容忍）

作者：后端
最后更新：2025-10-01（本次更新：引入 deviceId，按设备并发控制）
状态：已落地
适用范围：会话并发控制、IP 近似设备数、Redis ZSET + 分布式锁

维护人：后端
关键代码路径：
- 分布式锁抽象：src/main/java/org/xhy/community/infrastructure/lock/*
- 设备会话领域：src/main/java/org/xhy/community/domain/session/service/*

> 重要升级说明（2025-10-01）：
> - 新增以 deviceId 为主的“按设备并发”控制，兼容旧的“按 IP 并发”方案；
> - 同一设备允许有限个活跃 IP（默认 3），以容忍梯子/切网；
> - 有效设备上限优先采用用户个性化设置（UserEntity.maxConcurrentDevices），为空则回退系统默认（UserSessionConfig.defaultMaxDevices，兼容旧字段 maxActiveIps）；
> - 客户端需在登录与后续请求携带 deviceId：请求头 X-Device-ID 或 Cookie DID；
> - 鉴权拦截器优先校验 deviceId 是否活跃；缺失 deviceId 时退化为按 IP 校验。

新增配置：src/main/java/org/xhy/community/domain/config/valueobject/UserSessionConfig.java
- `maxIpsPerDevice`：同设备允许的活跃 IP 上限（默认 3）。

新增/调整接口：
- 登录请求增加可选字段 `deviceId`（兼容旧客户端为空的场景）。
- 每次请求建议携带 `X-Device-ID` 请求头或 `DID` Cookie。


## 1. 背景与目标

- 目标：通过“并发活跃 IP 数”来近似限制“并发活跃设备数”，防止同一账号多处同时在线。
- 简化原则：以 IP 作为唯一识别依据，不采集或依赖设备指纹。若同账号出现新的 IP，则判定为新的设备来源；默认策略为淘汰最久未活跃的旧会话（LRU）或拒绝新登录（可配置）。
- 适配范围：Web、App、桌面端统一按 IP 执行限制；认证/鉴权链路不变，仅在会话管理环节接入。

## 2. 术语与口径

- 活跃会话：状态为 ACTIVE 且未过期的用户登录会话。
- 并发活跃 IP 数：在用户的所有活跃会话中，按 IP 去重后的计数。
- 设备数近似：以“并发活跃 IP 数”作为设备数的等价口径。
- LRU 淘汰：当新 IP 登录超出配额时，优先下线最久未活跃（lastSeen 最小）的会话。

## 3. 方案概述

1) 基于 Redis 统计并控制“并发活跃 IP 数”：使用 ZSET 维护当前活跃 IP 及其最近活跃时间，用于容量控制与 LRU 淘汰；使用 ZSET 维护历史 IP 的最近出现时间，用于滑窗统计与封禁。
2) 核心约束：同一用户的“活跃设备数”不得超过 `defaultMaxDevices`（历史配置字段 `maxActiveIps` 兼容为默认设备上限）。
3) 新 IP 进入：
   - 策略 DENY_NEW：直接拒绝登录（返回“设备数超限”）。
   - 策略 EVICT_OLDEST：按照 LRU 淘汰最久未活跃的 IP，再接受新 IP。
4) 同 IP 重复登录：视作同一来源，仅续活（更新 lastSeen 分值），不增加并发计数。
5) 并发串行化：通过“分布式锁抽象（Redis 实现）”将同一用户的变更串行，避免竞态导致超配额。
6) 可选持久化：如需审计/后台可视化，可将会话或事件持久化到 DB；不影响 Redis 实时控制面。

## 4. 数据模型（DDD/DB）

### 4.1 实体与仓储

- `DeviceSessionEntity`（领域实体，继承 BaseEntity）
  - 字段：`id, userId, ip, userAgentHash, loginTime, lastSeenTime, expiresAt, status`
  - 说明：只基于 IP 进行设备限制；`userAgentHash` 仅用于审计与可视化。
- `UserDeviceQuotaEntity`（可选，用户定制配额）
  - 字段：`userId, maxIps, policy(DENY_NEW/EVICT_OLDEST)`

仓储接口：
- `DeviceSessionRepository extends BaseMapper<DeviceSessionEntity>`
- `UserDeviceQuotaRepository extends BaseMapper<UserDeviceQuotaEntity>`

### 4.2 表结构（Flyway 迁移建议，PostgreSQL）

- 表：`user_device_sessions`
  - `id varchar(36) primary key`
  - `user_id varchar(36) not null`
  - `ip inet not null`（或 `varchar(64)`，推荐 `inet`）
  - `user_agent_hash varchar(64)`
  - `login_time timestamp not null`
  - `last_seen_time timestamp not null`
  - `expires_at timestamp not null`
  - `status varchar(16) not null`（ACTIVE/REVOKED/EXPIRED）
  - `deleted boolean default false`
  - `create_time timestamp not null, update_time timestamp not null`
  - 索引：
    - `btree(user_id, status)` 覆盖活跃会话查询
    - `btree(user_id, last_seen_time desc)` 支持 LRU 淘汰

- 表：`user_device_quotas`（可选）
  - `user_id varchar(36) primary key`
  - `max_ips int not null`
  - `policy varchar(16) not null`（DENY_NEW/EVICT_OLDEST）

## 5. Redis 数据结构与并发控制

为实现多设备并发、LRU 淘汰与滑窗封禁，采用如下键设计（按用户维度）：

- `u:{userId}:active_ips` ZSET
  - member：IP 字符串；score：lastSeen 毫秒时间戳
  - 功能：并发活跃 IP 去重、按 `ZCARD` 控制容量、按 `ZPOPMIN` 做 LRU 淘汰
  - 清理：每次登录/心跳时执行 `ZREMRANGEBYSCORE active_ips -inf (now - sessionTtlMs)` 清除失效成员

- `u:{userId}:ip_history` ZSET
  - member：IP 字符串；score：最近出现的毫秒时间戳
  - 功能：滑窗去重 IP 统计，用于判定账号共享并封禁
  - 清理：`ZREMRANGEBYSCORE ip_history -inf (now - historyWindowMs)`；`ZCARD` 与阈值比较

- `u:{userId}:ban` String/Flag（可选，带 TTL）
  - 功能：封禁标记；在登录与鉴权阶段提前拦截

- `lock:user:{userId}:ip` 分布式锁
  - 功能：在“读→判断→淘汰→写→统计→可能封禁”的临界区内串行化，避免竞态
  - 建议：等待时长 100–300ms；租约 3–5s；避免长临界区

回源与修复（可选）：若需以 DB 为真，允许定期任务对 Redis 进行纠偏；普通场景下 Redis 可作为控制面的唯一真相源。

> 注：按项目约定“使用分布式锁而非 Lua 原子脚本”。加锁后多个 Redis 操作在同一临界区内是串行一致的，满足业务原子性需求。

## 5.1 分布式锁抽象设计

- 接口位置建议：`org.xhy.community.infrastructure.lock`
- 设计目标：解耦业务与具体实现；默认提供 Redis 实现，未来可替换为 DB/Etcd/Zookeeper 实现。

接口示例：
```java
// 文件：src/main/java/org/xhy/community/infrastructure/lock/DistributedLock.java
package org.xhy.community.infrastructure.lock;

import java.time.Duration;
import java.util.function.Supplier;

public interface DistributedLock {
    <T> T executeWithLock(String key, Duration waitTime, Duration leaseTime, Supplier<T> supplier);
    void runWithLock(String key, Duration waitTime, Duration leaseTime, Runnable runnable);
}
```

Redis 实现建议：
- `RedisDistributedLock implements DistributedLock`
- 基于 Redisson 或原生实现（SET NX PX + 唯一 owner 标识 + 定时释放）
- 统一封装重入策略（可不支持重入），确保释放仅限 owner 自己

使用示例（领域服务中）：
```java
deviceLock.runWithLock("lock:user:" + userId + ":ip", Duration.ofMillis(200), Duration.ofSeconds(5), () -> {
    // 读取/清理 active_ips 过期成员
    // 判断是否需要淘汰，执行 ZPOPMIN 再 ZADD 新 IP
    // 更新 ip_history 并滑窗清理；超过阈值则设置 ban 标记
});
```

### 5.2 Redis 分布式锁实现骨架

```java
// 文件：src/main/java/org/xhy/community/infrastructure/lock/RedisDistributedLock.java
package org.xhy.community.infrastructure.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RedisDistributedLock implements DistributedLock {
    private final RedissonClient redissonClient;

    public RedisDistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T executeWithLock(String key, Duration wait, Duration lease, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(wait.toMillis(), lease.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new IllegalStateException("获取分布式锁失败: " + key);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("锁等待被中断: " + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void runWithLock(String key, Duration wait, Duration lease, Runnable runnable) {
        executeWithLock(key, wait, lease, () -> { runnable.run(); return null; });
    }
}
```

> 若不引入 Redisson，可使用 `StringRedisTemplate` + `SET NX PX` 自行实现，需注意：
> - 使用随机 `ownerId` 标识锁持有者，释放时校验持有者一致；
> - 释放锁建议使用原子脚本或 Lua，以避免“误释放”他人锁（此处仅作为释放动作，非业务原子流程，不与本方案的“避免 Lua 做多命令原子业务逻辑”相冲突）。

## 6. 核心流程

### 6.1 登录/上线（加锁串行）

1) 读取配额：全局默认或套餐级覆盖（`defaultMaxDevices/policy`）
2) 获取分布式锁：`lock:user:{userId}:ip`
3) 失效清理：`ZREMRANGEBYSCORE active_ips -inf (now - sessionTtlMs)`
4) 历史滑窗：`ZREMRANGEBYSCORE ip_history -inf (now - historyWindowMs)`；`ZADD ip_history ip now`；`ZCARD` 与 `banThreshold` 比较，超阈值→设置 `ban` 并拒绝
5) 新 IP 判定：若 `ZSCORE active_ips ip` 存在 → `ZADD active_ips ip now` 续活；否则：
   - 若 `ZCARD active_ips < defaultMaxDevices` → `ZADD active_ips ip now`
   - 否则按策略：
     - DENY_NEW → 拒绝
     - EVICT_OLDEST → `ZPOPMIN active_ips` 淘汰最老，再 `ZADD active_ips ip now`
6) 释放锁；后续签发 Token

### 6.2 请求通过认证（心跳/续活）

- 鉴权前检查封禁：存在 `u:{userId}:ban` → 直接拒绝
- 校验：请求来源 IP 必须存在于 `active_ips`（`ZSCORE` 存在性），否则 401
- 续活（按节流，如 60s 一次）：
  - 获取锁，`ZADD active_ips ip now`；同时执行 6.1 的步骤 3/4 做失效清理与滑窗维护

### 6.3 登出/强制下线

- 用户主动：当前 `sessionId` 状态改为 REVOKED，并从 `u:{userId}:ips` 的引用关系中回收（若同 IP 不再被其他会话使用，则从 Set 移除）。
- 管理员介入：`AdminSessionAppService` 支持分页查看活跃会话并强制下线。

## 7. 领域层职责与接口（DDD）

- `DeviceSessionDomainService`（只做业务规则，不做参数格式校验）
  - `createOrReuseByIp(String userId, String ip, String userAgentHash, LocalDateTime expiresAt)`
  - `touchSession(String sessionId, String ip)`
  - `revokeSession(String userId, String sessionId)`
  - `listActiveSessions(String userId)`
  - 规则点：
    - 基于“并发活跃 IP 数”限制；
    - 策略：`DENY_NEW` 或 `EVICT_OLDEST`；
    - LRU 淘汰选择；
    - 过期会话 EXPIRED 定期清理。

- 仓储：
  - `DeviceSessionRepository` 使用 MyBatis Plus 条件构造器查询活跃会话与分页，不写自定义 SQL。

### 7.1 领域服务使用分布式锁（直接操作 Redis）的示例

```java
// 文件：src/main/java/org/xhy/community/domain/session/service/DeviceSessionDomainService.java
package org.xhy.community.domain.session.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.lock.DistributedLock;

import java.time.Duration;
import java.util.Set;

@Service
public class DeviceSessionDomainService {
    private final DistributedLock deviceLock;
    private final StringRedisTemplate redis;

    public DeviceSessionDomainService(DistributedLock deviceLock, StringRedisTemplate redis) {
        this.deviceLock = deviceLock;
        this.redis = redis;
    }

    public boolean createOrReuseByIp(String userId, String ip,
                                     int maxActiveIps, EvictPolicy policy,
                                     long sessionTtlMs, long historyWindowMs,
                                     int banThreshold, long banTtlMs) {
        String lockKey = "lock:user:" + userId + ":ip";
        String activeKey = "u:" + userId + ":active_ips";
        String histKey = "u:" + userId + ":ip_history";
        String banKey = "u:" + userId + ":ban";

        return deviceLock.executeWithLock(lockKey, Duration.ofMillis(200), Duration.ofSeconds(5), () -> {
            long now = System.currentTimeMillis();

            // 1) 封禁检查
            Boolean banned = redis.hasKey(banKey);
            if (Boolean.TRUE.equals(banned)) {
                return false;
            }

            // 2) 清理过期活跃 IP
            redis.opsForZSet().removeRangeByScore(activeKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);

            // 3) 历史滑窗维护与封禁判定
            redis.opsForZSet().removeRangeByScore(histKey, Double.NEGATIVE_INFINITY, now - historyWindowMs);
            redis.opsForZSet().add(histKey, ip, now);
            Long histCount = redis.opsForZSet().zCard(histKey);
            if (histCount != null && histCount > banThreshold) {
                if (banTtlMs > 0) {
                    redis.opsForValue().set(banKey, "1", Duration.ofMillis(banTtlMs));
                } else {
                    redis.opsForValue().set(banKey, "1");
                }
                return false;
            }

            // 4) 并发活跃 IP 控制
            Double score = redis.opsForZSet().score(activeKey, ip);
            if (score != null) {
                // 续活
                redis.opsForZSet().add(activeKey, ip, now);
                return true;
            }

            Long activeCount = redis.opsForZSet().zCard(activeKey);
            long count = activeCount == null ? 0 : activeCount;
            if (count < maxActiveIps) {
                redis.opsForZSet().add(activeKey, ip, now);
                return true;
            }

            if (policy == EvictPolicy.DENY_NEW) {
                return false;
            }

            // LRU 淘汰最老的一个 IP
            Set<ZSetOperations.TypedTuple<String>> oldest = redis.opsForZSet().rangeWithScores(activeKey, 0, 0);
            if (oldest != null && !oldest.isEmpty()) {
                String victim = oldest.iterator().next().getValue();
                if (victim != null) {
                    redis.opsForZSet().remove(activeKey, victim);
                }
            }
            redis.opsForZSet().add(activeKey, ip, now);
            return true;
        });
    }
}

enum EvictPolicy { DENY_NEW, EVICT_OLDEST }
```

## 8. 配置项（Infrastructure.Config）

```yaml
user:
  session:
    max-active-ips: 1              # 默认最大并发 IP（即最大设备数）
    policy: EVICT_OLDEST           # 或 DENY_NEW
    ttl-days: 30                   # 会话有效期（天）
    history-window-seconds: 2592000 # 历史滑窗，默认30天
    ban-threshold: 10              # 滑窗内去重 IP 超过阈值即封禁
    ban-ttl-days: 7                # 封禁时长（天），可为0表示永久
    touch-interval-seconds: 60     # lastSeen 刷新节流
  lock:
    wait-millis: 200               # 分布式锁等待时长
    lease-millis: 5000             # 分布式锁租约（自动释放）
```

## 9. 认证链路集成

- 登录成功编排（App 层）：调用 `DeviceSessionDomainService.createOrReuseByIp(...)` 完成配额判断与活跃 IP 维护，再签发 Token。
- 鉴权过滤器（Infra 层）：
  - 获取可信客户端 IP（正确配置并验证 `X-Forwarded-For`/`X-Real-IP` 来源）
  - 检查封禁标记；校验该 IP 是否存在于 `active_ips`，否则 401
  - 通过后按节流触发续活（可异步）
- 下线处理：用户/管理员接口触发“移除某 IP”或“清空活跃 IP”，后续请求即 401。

## 10. 运维与清理

- 定期任务：扫描 `expiresAt < now()` 或长期未活跃会话，标记 EXPIRED 并回收缓存集合。
- 监控指标：
  - 登录被拒次数（超配额）
  - 自动淘汰次数（LRU）
  - 活跃会话数与活跃 IP 去重数
  - 缓存回源/重建次数

- Redis 清理：
  - `active_ips`：按会话 TTL 定期 `ZREMRANGEBYSCORE` 清理过期成员
  - `ip_history`：按滑窗定期 `ZREMRANGEBYSCORE` 清理窗口外成员

## 11. 风险与取舍（仅记录，不改变本方案）

- NAT/共享网络：同网下多终端共享同一外网 IP，会被视为同“设备”，可能放宽限制。
- 移动网络漂移：运营商切换导致频繁变更 IP，可能触发误判与淘汰；可通过 LRU 策略相对平滑。
- 代理/VPN：用户切换代理即视为新“设备”，如配额较小将引发频繁淘汰。

> 上述风险为 IP 口径的天然限制，已按需求接受该简化前提；后续如需增强可引入设备指纹作为辅因子。

## 12. 迭代与落地步骤

1) 基线实现（Redis-only 控制面）：分布式锁抽象 + Redis 实现；`active_ips/ip_history/ban` 维护逻辑；鉴权校验与续活。
2) 配置与监控：落地配置项、指标埋点、报警规则。
3) 管理能力：用户自助查看/下线；管理员查看/强制下线；套餐配额读取集成。
4) 可选持久化：如需，新增 Flyway 脚本与审计持久化（不影响控制面）。
