package org.xhy.community.interfaces.oauth2.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 获取用户授权列表请求
 * 用户查询自己已授权的第三方应用列表，支持分页
 */
public class GetUserAuthorizationsRequest extends PageRequest {

    public GetUserAuthorizationsRequest() {
    }

    public GetUserAuthorizationsRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}
