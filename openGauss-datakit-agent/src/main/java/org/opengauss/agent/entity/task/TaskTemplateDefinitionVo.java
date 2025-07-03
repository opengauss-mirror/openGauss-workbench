/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.entity.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opengauss.agent.enums.ObjectType;
import org.opengauss.agent.enums.StoragePolicy;
import org.opengauss.agent.enums.TaskType;

/**
 * TaskTemplateDefinitionVo
 *
 * @author: wangchao
 * @Date: 2025/4/16 09:34
 * @Description: TaskTemplateDefinitionVo
 * @since 7.0.0-RC2
 **/
@Data
@EqualsAndHashCode
public class TaskTemplateDefinitionVo {
    private String name;
    private TaskType type;
    private String groupTag;
    private String pluginsTag;
    private ObjectType operateObjType;
    private String operateObj;

    /**
     * <pre>
     * Obtains a Duration from a text string such as PnDTnHnMn.nS.
     * time period and duration ,1year 2month 3week 4day 5hour 6minutes 7.5second eg. "P1Y2M3W4DT5H6M7.5S"
     *
     * The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days considered to be exactly
     * 24 hours.
     * For example, P1Y2M3W4DT5H6M7S is a valid duration.
     * A date-based amount of time in the ISO-8601 calendar system, such as '2 years, 3 months and 4 days'.
     * </pre>
     */
    private String period;
    private String collectMetric;
    private StoragePolicy storagePolicy;

    /**
     * <pre>
     * Obtains a Duration from a text string such as PnDTnHnMn.nS.
     * time period and duration,1year 2month 3week 4day 5hour 6minutes 7.5second eg.
     * time period and duration,1year 2month 3week 4day 5hour 6minutes 7.5second eg. "P1Y2M3W4DT5H6M7.5S"
     *
     * The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days considered to be exactly
     * 24 hours.
     * For example,      * For example, P1Y2M3W4DT5H6M7S is a valid duration.
     * A date-based amount of time in the ISO-8601 calendar system, such as '2 years, 3 months and 4 days'.
     * </pre>
     */
    private String keepPeriod;

    /**
     * task estimated execution time , unit is milliseconds
     */
    private long estimatedExecutionTime;
    private String receiveApi;
}
