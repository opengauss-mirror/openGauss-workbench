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

import org.junit.Test;
import org.opengauss.agent.repository.TableNameGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TableNameGeneratorTest
 *
 * @author: wangchao
 * @Date: 2025/5/28 09:46
 * @since 7.0.0-RC2
 **/
public class TableNameGeneratorTest {
    @Test
    public void testGenerateTableName() {
        List<String> taskIds = List.of("test", "datakit_agent_pip_test", "opengauss_instance_memory_top10",
            "modeling_visualization_custom_dimensions", "ops_az", "base_host_info", "host_dynamic_metrics",
            "opengauss_instance_monitor", "opengauss_alarm_notification", "opengauss_slow_sql_query");
        Set<String> tableNameSet = new HashSet<>();
        for (String taskId : taskIds) {
            for (int i = 0; i < 1000000; i++) {
                String realTimeTableName = TableNameGenerator.getRealTimeTableName(taskId);
                tableNameSet.add(realTimeTableName);
            }
        }
        assertEquals(taskIds.size(), tableNameSet.size());
    }
}
