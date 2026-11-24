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

    /**
     * platform
     */
    private String platform;

    /**
     * jobIds
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> jobIds;

    /**
     * targets
     */
    private List<SysJob> targets;

    /**
     * lastReleaseTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date lastReleaseTime;

    private Long time;

    private Boolean isCreate;
}
