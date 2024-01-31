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

package org.opengauss.tun.domain.builder;

import cn.hutool.core.util.StrUtil;

/**
 * TuningBuilder
 *
 * @author liu
 * @since 2023-12-05
 */
public class TuningBuilder {
    private StringBuilder sb = new StringBuilder();

    /**
     * appendProperty
     *
     * @param key   key
     * @param value value
     * @return AssessmentBuilder Builder
     */
    public TuningBuilder appendProperty(String key, String value) {
        if (StrUtil.isNotEmpty(value)) {
            sb.append(key).append(" = ").append(value).append(StrUtil.C_LF);
        }
        return this;
    }

    /**
     * appendNote
     *
     * @param note note
     * @return  TuningBuilder
     */
    public TuningBuilder appendNote(String note) {
        if (StrUtil.isNotEmpty(note)) {
            sb.append(note).append(StrUtil.C_LF);
        }
        return this;
    }

    /**
     * appendSectionBreak
     *
     * @return AssessmentBuilder builder
     */
    public TuningBuilder appendSectionBreak() {
        sb.append(StrUtil.C_LF);
        return this;
    }

    /**
     * build
     *
     * @return String str
     */
    public String build() {
        return sb.toString();
    }
}
