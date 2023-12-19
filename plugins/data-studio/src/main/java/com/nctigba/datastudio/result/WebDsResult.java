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
 *  WebDsResult.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/result/WebDsResult.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.result;

import lombok.Data;

import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.TWO_HUNDRED;

/**
 * WebDsResult
 *
 * @since 2023-6-26
 */
@Data
public class WebDsResult {
    private String type;

    private String code;

    private String msg;

    private Object data;

    private String error;

    /**
     * ok
     *
     * @param type type
     * @param statusMsg statusMsg
     * @return WebDsResult
     */
    public static WebDsResult ok(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode(TWO_HUNDRED);
        result.setMsg(statusMsg);
        return result;
    }

    /**
     * error
     *
     * @param type type
     * @param statusMsg statusMsg
     * @return WebDsResult
     */
    public static WebDsResult error(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode(FIVE_HUNDRED);
        result.setMsg(statusMsg);
        return result;
    }

    /**
     * add data
     *
     * @param object object
     * @return WebDsResult
     */
    public WebDsResult addData(Object object) {
        this.setData(object);
        return this;
    }
}