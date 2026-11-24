/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.instance.model.dto.cluster;

import com.nctigba.observability.instance.enums.StateColor;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * State
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateDTO {
    private String value;
    private StateColor color;

    /**
     * Constructor
     *
     * @param value value
     */
    public StateDTO(String value) {
        this.value = value;
        setColor();
    }

    public void setColor() {
        this.color = StateColor.getColor(value);
    }

    public String getValue() {
        String ret = MessageSourceUtils.getMsg(value);
        if (ret == null) {
            String[] split = value.split("\\.");
            ret = split[split.length - 1];
        }
        return ret;
    }
}
