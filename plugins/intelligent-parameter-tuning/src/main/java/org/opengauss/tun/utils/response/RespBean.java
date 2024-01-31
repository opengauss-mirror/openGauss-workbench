/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RespBean
 *
 * @author liu
 * @since 2023-09-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {
    private Integer code;

    private String msg;

    @JsonInclude(Include.NON_NULL)
    private Object obj;

    private int total;

    /**
     * RespBean
     *
     * @param code code
     * @param msg msg
     * @param obj obj
     */
    public RespBean(Integer code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    /**
     * 成功返回结果
     *
     * @param msg msg
     * @return RespBean RespBean
     */
    public static RespBean success(String msg) {
        return new RespBean(200, msg, null);
    }

    /**
     * 成功返回结果
     *
     * @param msg msg
     * @param obj obj
     * @return RespBean RespBean
     */
    public static RespBean success(String msg, Object obj) {
        return new RespBean(200, msg, obj);
    }

    /**
     * success
     *
     * @param msg msg
     * @param obj obj
     * @param total total
     * @return RespBean
     */
    public static RespBean success(String msg, Object obj, int total) {
        return new RespBean(200, msg, obj, total);
    }

    /**
     * 失败返回结果
     *
     * @param msg msg
     * @return RespBean RespBean
     */
    public static RespBean error(String msg) {
        return new RespBean(500, msg, null);
    }

    /**
     * 失败返回结果
     *
     * @param msg msg
     * @return RespBean RespBean
     */
    public static RespBean startError(String msg) {
        return new RespBean(600, msg, null);
    }

    /**
     * 失败返回结果
     *
     * @param msg msg
     * @param obj obj
     * @return RespBean RespBean
     */
    public static RespBean error(String msg, Object obj) {
        return new RespBean(500, msg, obj);
    }
}