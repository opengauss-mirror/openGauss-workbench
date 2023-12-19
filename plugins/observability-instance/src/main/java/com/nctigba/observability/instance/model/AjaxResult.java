/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AjaxResult.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/AjaxResult.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.opengauss.admin.common.enums.ResponseCode;

/**
 * Ajax Result Response
 *
 * @since 2023/12/1
 */
@Data
@ApiModel("Unified result return format")
public class AjaxResult<T> {
    @ApiModelProperty(value = "Result code.200 means ok,500 means error.", allowableValues = "200,500", required = true)
    private Integer code;
    @ApiModelProperty(value = "Result msg, return when 500.", required = true)
    private String msg;
    @ApiModelProperty(value = "Business data", required = true)
    private T data;

    /**
     * AjaxResult()
     *
     * @return AjaxResult
     */
    public AjaxResult() {
        this(200, null);
    }

    /**
     * Use code and msg
     *
     * @param code Result code
     * @param msg Result msg
     * @return AjaxResult
     */
    public AjaxResult(int code, String msg) {
        this(code, msg, null);
    }

    /**
     * Use code, msg and data
     *
     * @param code Result code
     * @param msg Result msg
     * @param data Result data
     * @return AjaxResult
     */
    public AjaxResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * Success result
     *
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult success() {
        return AjaxResult.success("success");
    }

    /**
     * Success result with data
     *
     * @param data Data
     * @return com.nctigba.observability.instance.model.AjaxResult<T>
     */
    public static <T> AjaxResult<T> success(T data) {
        return AjaxResult.success("success", data);
    }


    /**
     * Success result with message
     *
     * @param msg Message
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * Success result with message and data
     *
     * @param msg Message
     * @param data Data
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(ResponseCode.SUCCESS.code(), msg, data);
    }

    /**
     * Error result
     *
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult error() {
        return AjaxResult.error("fail");
    }

    /**
     * Error result with message
     *
     * @param msg Message
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * Error result with code
     *
     * @param code Code
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult error(Integer code) {
        return AjaxResult.error(code, ResponseCode.getInstance(code).msg());
    }

    /**
     * error response encapsulation
     *
     * @param code error code
     * @param msg error message
     * @param data data
     * @return Response Result
     */
    public static AjaxResult errorAttachedData(Integer code, String msg, Object data) {
        return new AjaxResult(code, msg, data);
    }

    /**
     * Error result with message and data
     *
     * @param msg Message
     * @param data Data
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(ResponseCode.ERROR.code(), msg, data);
    }

    /**
     * Error result with code and message
     *
     * @param code Code
     * @param msg Message
     * @return com.nctigba.observability.instance.model.AjaxResult
     */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    /**
     * Check if result is ok
     *
     * @return boolean
     */
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public boolean isOk() {
        return code != null && code.equals(ResponseCode.SUCCESS.code());
    }
}
