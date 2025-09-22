/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.entity;

import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * ProcessedData
 *
 * @author: wangchao
 * @Date: 2025/2/28 11:50
 * @Description: ProcessedData
 * @since 7.0.0-RC2
 **/
@Data
public class ProcessedData {
    String message;

    /**
     * Constructor
     *
     * @param raw RawData
     */
    public ProcessedData(RawData raw) {
        this.message = raw.getMessage();
    }

    /**
     * toJson
     *
     * @return String
     */
    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }

    /**
     * fromJson
     *
     * @param json String
     * @return ProcessedData
     */
    public static ProcessedData fromJson(String json) {
        return JSONUtil.toBean(json, ProcessedData.class);
    }
}
