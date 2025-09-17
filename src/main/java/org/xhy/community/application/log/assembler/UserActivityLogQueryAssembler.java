package org.xhy.community.application.log.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.domain.log.query.UserActivityLogQuery;
import org.xhy.community.interfaces.log.request.QueryUserActivityLogRequest;

/**
 * 用户活动日志转换器
 * 负责请求对象与查询对象之间的转换
 */
public class UserActivityLogQueryAssembler {
    
    /**
     * 将请求对象转换为查询对象
     * 
     * @param request 查询请求对象
     * @return 查询对象
     */
    public static UserActivityLogQuery toQuery(QueryUserActivityLogRequest request) {
        if (request == null) {
            return null;
        }
        
        UserActivityLogQuery query = new UserActivityLogQuery();
        BeanUtils.copyProperties(request, query);
        return query;
    }
}