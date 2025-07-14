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

package org.opengauss.admin.system.service.agent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.codec.digest.MurmurHash3;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * MurmurHash3Test
 *
 * @author: wangchao
 * @Date: 2025/5/28 17:17
 * @since 7.0.0-RC2
 **/
public class MurmurHash3Test {
    @Test
    public void testGenerateTableName() {
        Set<Long> ids = new HashSet<>();
        Long taskId = 123L;
        Long agentId = 456L;
        String clusterId = "cluster1";
        for (int i = 0; i < 10; i++) {
            ids.add(calcRowId(taskId, agentId, clusterId, i));
        }
        assertEquals(10, ids.size());
    }

    private long calcRowId(Long taskId, Long agentId, String clusterId, int rowIdx) {
        return MurmurHash3.hash32x86((taskId + agentId + clusterId + rowIdx).getBytes(StandardCharsets.UTF_8));
    }
}
