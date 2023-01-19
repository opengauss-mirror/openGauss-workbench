package com.tools.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tools.monitor.quartz.domain.SysJob;
import lombok.Data;

import java.util.Date;
import java.util.List;


/**
 * SysConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SysConfig {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;

    private String connectName;

    private String ip;

    private String containerIp;

    private String containerPort;

    private String serverIp;

    private String clientIp;

    private String port;

    private String driver;

    private String userName;

    private String serverName;

    private String clientName;

    private String password;

    private String serverPassword;

    private String clientPassword;

    private String clientPath;

    private String serverPath;

    private String url;

    private String createTime;

    private String dataBaseName;

    private String platform;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> jobIds;

    private List<SysJob> targets;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date lastReleaseTime;

    private Long time;

    private Boolean isCreate;
}
