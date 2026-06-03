package com.lessonplan.entity.vo;

import java.io.Serializable;

/**
 * 统一响应VO
 */
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private Integer code;
    private String info;
    private T data;

    public ResponseVO() {
    }

    public ResponseVO(String status, Integer code, String info, T data) {
        this.status = status;
        this.code = code;
        this.info = info;
        this.data = data;
    }

    public static <T> ResponseVO success(T data) {
        return new ResponseVO("success", 200, null, data);
    }

    public static <T> ResponseVO error(Integer code, String info) {
        return new ResponseVO("error", code, info, null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
