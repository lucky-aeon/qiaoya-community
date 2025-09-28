# Nginx/Traefik 蓝绿切流运维操作手册

作者：后端/运维
最后更新：2025-09-27
状态：实践指引（可直接执行）
适用范围：单机或多机 Docker 部署，前置代理为 Nginx 或 Traefik

关联文档：
- 发布可靠性与回滚策略：docs/deployment/发布可靠性与回滚策略-蓝绿-金丝雀-候选容器预检.md
- CI/CD 方案（GitHub Actions + SSH + Docker）：docs/deployment/后端CI-CD技术方案-GitHub-Actions-SSH-Docker.md

---

## 1. 蓝绿拓扑与前置条件

- 两套后端容器同时运行：
  - blue（旧版本）：监听 8520
  - green（新版本）：监听 8521
- 前置代理（Nginx/Traefik）对外只暴露固定端口/域名，后端真实端口通过 upstream 权重或路由进行切换。
- 应用健康检查统一使用 `/actuator/health`，切流前确保 green 为 UP。

容器命名建议：
- `qiaoya-community-backend-blue`
- `qiaoya-community-backend-green`

---

## 2. 上线/切流总流程（蓝绿）

1) 启动 green 容器（新镜像），映射到 8521，注入与生产一致的 env-file。
2) 进行候选容器预检：
   - 轮询 `http://127.0.0.1:8521/actuator/health`，状态为 `UP`。
   - 可选：执行关键接口冒烟（公开接口即可）。
3) 切流：在代理（Nginx/Traefik）将流量权重从 blue 指向 green。
4) 观察 5~10 分钟：错误率、P95 延迟、业务 KPI。
5) 稳定后：下线 blue（或保留一段时间作为回退备份）。
6) 异常回滚：立即把权重切回 blue，green 保留排障。

---

## 3. Nginx 操作

示例 upstream（blue:8520、green:8521）：
```
upstream qiaoya_backend {
    server 127.0.0.1:8520 weight=100 max_fails=3 fail_timeout=10s; # blue
    server 127.0.0.1:8521 weight=0;                                # green
}

server {
    listen 80;
    server_name api.example.com;

    proxy_connect_timeout 5s;
    proxy_read_timeout    60s;
    proxy_send_timeout    60s;
    proxy_http_version    1.1;
    proxy_set_header      Host $host;
    proxy_set_header      X-Real-IP $remote_addr;
    proxy_set_header      X-Forwarded-For $proxy_add_x_forwarded_for;

    location / {
        proxy_pass http://qiaoya_backend;
    }
}
```

切流步骤：
- 蓝绿切换：将 green 改为 `weight=100`，blue 改为 `weight=0`。
- 金丝雀：按 95/5 → 90/10 → 50/50 → 100/0 逐步放量。

命令：
- 语法检查：`nginx -t`
- 平滑加载：`nginx -s reload` 或 `systemctl reload nginx`
- 日志观测：`journalctl -u nginx -f` 或查看 access/error 日志

回滚：
- 反向调整权重（blue=100，green=0）→ `nginx -t` → reload。

常见问题：
- 502/504：检查后端容器是否就绪；延长 `proxy_*_timeout`；确认健康检查通过。
- 客户端 IP 丢失：确保 `X-Real-IP`、`X-Forwarded-For` 头已传递。

---

## 4. Traefik 操作

静态文件（traefik.yml）：
```
entryPoints:
  web:
    address: ":80"
providers:
  file:
    filename: /etc/traefik/dynamic/qiaoya.yml
    watch: true
```

动态文件（/etc/traefik/dynamic/qiaoya.yml）：
```
http:
  routers:
    qiaoya:
      rule: Host(`api.example.com`)
      entryPoints: [ web ]
      service: qiaoya-weighted

  services:
    qiaoya-weighted:
      weighted:
        services:
          - name: qiaoya-blue
            weight: 100
          - name: qiaoya-green
            weight: 0

    qiaoya-blue:
      loadBalancer:
        servers:
          - url: "http://127.0.0.1:8520"

    qiaoya-green:
      loadBalancer:
        servers:
          - url: "http://127.0.0.1:8521"
```

切流步骤：
- 编辑动态文件，将权重按蓝绿/金丝雀策略调整并保存。
- Traefik 自动热更新（`watch: true`）。
- 验证：`curl -H "Host: api.example.com" http://127.0.0.1/actuator/health`。
- 观察指标与日志；异常回调权重并保存（即时回滚）。

可选增强：设置 `forwardingTimeouts`、`serversTransport`；如需会话粘滞，结合 JWT 无状态或 Sticky（权衡）。

---

## 5. 验收与回滚清单

- [ ] green 启动成功且 `/actuator/health` 为 UP
- [ ] 公开接口冒烟 2~3 条通过（如统计、评价、套餐）
- [ ] 代理切流权重修改生效（Nginx reload / Traefik 热更新）
- [ ] 5~10 分钟观察窗口内指标正常
- [ ] 回滚方案已演练：权重一键切回 blue

