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

package com.tools.monitor.quartz.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tools.monitor.common.contant.ScheduleCommon;
import com.tools.monitor.entity.BasicEntity;
import java.util.List;
import lombok.Data;

/**
 * SysJob
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SysJob extends BasicEntity {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long jobId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;

    private Boolean isCreate;

    private Boolean isFalse;

    private String target;

    private Integer num;

    private String timeType;

    private Long time;

    private String startTime;

    private String jobName = "sql";

    private String jobGroup = "DEFAULT";

    private String targetGroup;

    private String platform;

    private String invokeTarget;

    private String cronExpression;

    private String misfirePolicy = ScheduleCommon.MONITOR_IGNORE_MISFIRES;

    private String concurrent = "0";

    private String status;

    private Boolean isPbulish = false;

    private Boolean isCanUpdate;

    private List<String> column;

    private Boolean isManagement;

    private List<String> timeInterval;
}
