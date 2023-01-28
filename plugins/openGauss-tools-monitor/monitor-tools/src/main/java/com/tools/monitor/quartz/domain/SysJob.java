/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
