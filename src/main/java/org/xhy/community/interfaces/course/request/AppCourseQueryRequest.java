package org.xhy.community.interfaces.course.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 前台课程查询请求
 * 用于前台API查询课程列表
 */
public class AppCourseQueryRequest extends PageRequest {

    public AppCourseQueryRequest() {}

    public AppCourseQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}