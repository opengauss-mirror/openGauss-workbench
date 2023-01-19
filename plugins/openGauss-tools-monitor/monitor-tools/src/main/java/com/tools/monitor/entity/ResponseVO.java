package com.tools.monitor.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * ResponseVO
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ResponseVO implements Serializable {
    private static final long serialVersionUID = -6344662283532956039L;

    private Integer code;

    private String message;

    private Integer total;

    private Object data;

    private ResponseVO(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseVO(Integer code, String message, Integer total, Object data) {
        this.code = code;
        this.message = message;
        this.total = total;
        this.data = data;
    }

    /**
     * successResponseVO
     *
     * @param message
     * @return
     */
    public static ResponseVO successResponseVO(String message) {
        return new ResponseVO(ResponseCode.RESPONSE_SUCCESS.getCode(), message, "");
    }

    /**
     * successResponseVO
     *
     * @param data
     * @return
     */
    public static ResponseVO successResponseVO(Object data) {
        return new ResponseVO(ResponseCode.RESPONSE_SUCCESS.getCode(), ResponseCode.RESPONSE_SUCCESS.getRemark(), data);
    }

    /**
     * successResponseVO
     *
     * @param message
     * @param data
     * @return
     */
    public static ResponseVO successResponseVO(String message, Object data) {
        return new ResponseVO(ResponseCode.RESPONSE_SUCCESS.getCode(), message, data);
    }

    /**
     * pageResponseVO
     *
     * @param total
     * @param data
     * @return
     */
    public static ResponseVO pageResponseVO(int total, Object data) {
        return new ResponseVO(ResponseCode.RESPONSE_SUCCESS.getCode(),ResponseCode.RESPONSE_SUCCESS.getRemark(), total, data);
    }

    /**
     * unAuthResponseVO
     *
     * @return
     */
    public static ResponseVO unAuthResponseVO() {
        return new ResponseVO(ResponseCode.RESPONSE_UNAUTH.getCode(), ResponseCode.RESPONSE_UNAUTH.getRemark(), "");
    }

    /**
     * errorResponseVO
     *
     * @param errorMsg
     * @return
     */
    public static ResponseVO errorResponseVO(String errorMsg) {
        return new ResponseVO(ResponseCode.RESPONSE_ERROR.getCode(), errorMsg,"");
    }

    /**
     * errorTarget
     *
     * @param errorMsg
     * @return
     */
    public static ResponseVO errorTarget(String errorMsg) {
        return new ResponseVO(ResponseCode.RESPONSE_TARGET_ERR.getCode(), errorMsg, "");
    }

    /**
     * exceptionResponseVO
     *
     * @param exception
     * @return
     */
    public static ResponseVO exceptionResponseVO(Exception exception) {
        return new ResponseVO(ResponseCode.RESPONSE_EXCEPTION.getCode(), exception.getMessage(), "data");
    }
}
