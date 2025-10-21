package org.xhy.community.interfaces.oauth2.request;

/**
 * 更新OAuth2客户端请求对象
 * 继承创建请求，复用所有字段和校验规则
 */
public class UpdateOAuth2ClientRequest extends CreateOAuth2ClientRequest {
    // 修改对象直接继承创建对象，差异只有主键（在Controller层通过路径参数传入）
}
