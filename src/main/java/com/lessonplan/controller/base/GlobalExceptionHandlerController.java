package com.lessonplan.controller.base;

import com.lessonplan.entity.enums.ResponseCodeEnum;
import com.lessonplan.entity.vo.ResponseVO;
import com.lessonplan.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandlerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseVO handleException(Exception e) {
        logger.error("全局异常捕获", e);

        if (e instanceof NoHandlerFoundException) {
            return getErrorResponseVO(ResponseCodeEnum.CODE_404.getCode(), ResponseCodeEnum.CODE_404.getMsg());
        }
        if (e instanceof BusinessException) {
            BusinessException be = (BusinessException) e;
            Integer code = be.getCode() != null ? be.getCode() : ResponseCodeEnum.CODE_600.getCode();
            return getErrorResponseVO(code, be.getMessage());
        }
        if (e instanceof DuplicateKeyException) {
            return getErrorResponseVO(ResponseCodeEnum.CODE_601.getCode(), ResponseCodeEnum.CODE_601.getMsg());
        }
        if (e instanceof MethodArgumentNotValidException) {
            return getErrorResponseVO(ResponseCodeEnum.CODE_600.getCode(), "参数校验失败");
        }
        return getErrorResponseVO(ResponseCodeEnum.CODE_500.getCode(), ResponseCodeEnum.CODE_500.getMsg());
    }
}
