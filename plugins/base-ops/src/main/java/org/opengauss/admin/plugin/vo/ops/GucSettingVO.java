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

package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

@Data
public class GucSettingVO {
    private String name;
    private String value;
    private String unit;
    private String shortDesc;
    private String extraDesc;
    private String varType;
    private String enumVals;
    private String maxVal;
    private String minVal;
    private String defaultVal;
    private String context;
    // if this setting is changed by frontend, set to true
    private Boolean hasChanged;
}
