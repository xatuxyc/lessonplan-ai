package com.lessonplan.controller.base;

import com.lessonplan.entity.vo.ResponseVO;

/**
 * Controller基类
 * 提供通用的响应封装方法
 */
public class BaseController {

    protected <T> ResponseVO<T> getSuccessResponseVO(T t) {
        return new ResponseVO<>("success", 200, null, t);
    }

    protected <T> ResponseVO<T> getErrorResponseVO(Integer code, String info) {
        return new ResponseVO<>("error", code, info, null);
    }
}
