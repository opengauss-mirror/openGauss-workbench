/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * MigrationTaskDetail.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/beans/MigrationTaskDetail.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.beans;

import lombok.Data;

import java.util.Date;

/**
 * @className: MigrationTaskDetail
 * @author: xielibo
 * @date: 2023-01-18 13:58
 **/
@Data
public class MigrationTaskDetail {

    private String sourceResourceId;
    private String sourceResourceName;
    private String sourceNodeId;
    private String sourceDb;
    private String sourceDbHost;
    private String sourceDbPort;
    private String sourceDbUser;
    private String sourceDbPass;
    private String sourceDbUrl;

    private String targetResourceId;
    private String targetResourceName;
    private String targetNodeId;
    private String targetDb;
    private String targetDbHost;
    private String targetDbPort;
    private String targetDbUser;
    private String targetDbPass;

    private String runHostId;
    private String runHost;
    private String runPort;
    private String runUser;
    private String runPass;

    private String migrationOperations;
    private String paramsConfig;

    private Integer execStatus;
    private Integer mainTaskId;

    private Date createTime;

    private Date finishTime;

    private Date execTime;

    private String taskLog;
}
