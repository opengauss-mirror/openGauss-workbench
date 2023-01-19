package com.tools.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * SysSourceTarget
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SysSourceTarget {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> jobIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date lastReleaseTime;
}
