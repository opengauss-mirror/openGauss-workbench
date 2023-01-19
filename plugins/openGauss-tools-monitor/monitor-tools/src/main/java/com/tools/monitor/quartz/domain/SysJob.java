package com.tools.monitor.quartz.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tools.monitor.common.contant.ScheduleConstants;
import com.tools.monitor.entity.BaseEntity;
import java.util.List;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * SysJob
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SysJob extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long jobId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;

    private Boolean isCreate;

    private Boolean isFalse;

    private String target;

    @NotNull(message = "Collection time interval cannot be empty")
    private Integer num;

    @NotNull(message = "Unit cannot be empty")
    private String timeType;

    private Long time;

    private String startTime;

    private String jobName = "sql";

    private String jobGroup = "DEFAULT";

    @NotNull(message = "Indicator group cannot be empty")
    private String targetGroup;

    @NotNull(message = "Monitoring platform cannot be empty")
    private String platform;

    private String invokeTarget;

    private String cronExpression;

    private String misfirePolicy = ScheduleConstants.MISFIRE_IGNORE_MISFIRES;

    private String concurrent = "0";

    private String status;

    private Boolean isPbulish = false;

    private Boolean isCanUpdate;

    private List<String> column;

    private Boolean isManagement;

    private List<String> timeInterval;
}
