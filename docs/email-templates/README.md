# 邮件模板规范（Out-App）

目标：提供统一、可读、兼容主流邮箱客户端（Gmail/Outlook/Apple Mail等）的 HTML 邮件模板，方便后续在代码中替换占位符并发送。

关键原则
- 统一布局：头部品牌、主体内容、CTA 按钮、页脚说明，宽度约 620px。
- 内联样式 + table 布局：提升客户端兼容性。
- 不使用追踪参数：链接不带 UTM 等追踪参数（按当前要求）。
- 可替换占位符：所有动态文案与链接用 `{{PLACEHOLDER}}` 表示，便于后端渲染时替换。
- 放置 Logo：使用外链图片 `{{LOGO_URL}}`（推荐 CDN 静态资源）。
- 页脚包含“管理通知设置”链接：`{{MANAGE_NOTIFICATIONS_URL}}`。

文件一览
- new-follower.html 新关注者
- content-update.html 关注内容更新
- cdk-activated.html CDK 激活成功
- subscription-expiring.html 订阅即将过期
- comment.html 新评论

占位符说明（按模板使用）
- 通用：
  - `{{LOGO_URL}}` Logo 地址（HTTPS 外链，推荐 120×32 或等比例）
  - `{{MANAGE_NOTIFICATIONS_URL}}` 通知设置页链接
  - `{{RECIPIENT_NAME}}` 收件人称呼

- new-follower.html：
  - `{{FOLLOWER_NAME}}` 关注者昵称
  - `{{FOLLOWER_PROFILE_URL}}` 关注者主页链接

- content-update.html：
  - `{{AUTHOR_NAME}}` 作者昵称
  - `{{CONTENT_TYPE}}` 内容类型（如 Post/Course）
  - `{{CONTENT_TITLE}}` 内容标题
  - `{{CONTENT_URL}}` 内容链接

- cdk-activated.html：
  - `{{CDK_CODE}}` 兑换码
  - `{{ACTIVATION_TIME}}` 激活时间（yyyy-MM-dd HH:mm:ss）

- subscription-expiring.html：
  - `{{DAYS_REMAINING}}` 剩余天数
  - `{{RENEWAL_URL}}` 续费链接

- comment.html：
  - `{{COMMENTER_NAME}}` 评论者昵称
  - `{{TARGET_TYPE}}` 被评论内容类型（post/course 等）
  - `{{TARGET_TITLE}}` 被评论内容标题
  - `{{TRUNCATED_COMMENT}}` 评论内容（已截断）
  - `{{TARGET_URL}}` 被评论内容链接

样式约定
- 颜色：
  - 品牌主色：`#2563eb`（按钮/链接）
  - 正文文字：`#1f2937`
  - 次要文字：`#6b7280`
  - 背景色：`#f6f7f9`
  - 卡片背景：`#ffffff`
- 字体：系统字体栈，兼容中文：
  `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'WenQuanYi Micro Hei', 'Noto Sans CJK SC', 'Source Han Sans SC', sans-serif`
- 布局：外层全宽表格 + 居中内容表格，按钮使用 table 包裹，全部使用内联样式。

Logo 放置说明
- 推荐使用 HTTPS 外链 `{{LOGO_URL}}`（CDN/静态资源域名）。
- 如未来需要改成 CID 内联或 base64，可在基础设施层（邮件发送服务）处理，不影响模板结构。

预览与测试
- 直接在浏览器打开 .html 可预览基础效果。
- 邮箱客户端渲染可能存在差异，发送到 Gmail、Outlook、Apple Mail 等进行实际测试更可靠。

