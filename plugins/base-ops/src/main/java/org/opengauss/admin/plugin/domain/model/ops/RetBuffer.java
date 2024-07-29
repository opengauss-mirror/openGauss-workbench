/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * RetBuffer.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/RetBuffer.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * RetBuffer
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
public class RetBuffer {
    private StringBuffer buffer;
    @Getter
    private String clusterId;

    public RetBuffer(String clusterId) {
        this.clusterId = clusterId;
        buffer = new StringBuffer();
    }

    public String getRetBuffer() {
        return buffer.toString();
    }

    public void sendText(String text) {
        buffer.append(text).append(System.lineSeparator());
    }
}
