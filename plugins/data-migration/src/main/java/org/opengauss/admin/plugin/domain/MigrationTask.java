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
 * MigrationTask.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/domain/MigrationTask.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;
import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Data
@Builder
@TableName("tb_migration_task")
public class MigrationTask {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String sourceNodeId;
    private String sourceDb;
    private String sourceDbHost;
    private String sourceDbPort;
    private String sourceDbUser;
    private String sourceDbPass;

    private String targetNodeId;
    private String targetDb;
    private String targetDbHost;
    private String targetDbPort;
    private String targetDbUser;
    private String targetDbPass;

    private String runHostId;
    private String runHost;
    private Integer runPort;
    private String runUser;
    private String runPass;
    private String runHostname;

    private String migrationOperations;
    private Integer migrationModelId;
    private String migrationProcess;

    private String targetDbVersion;

    /**
     * true: adjust false: no adjust
     */
    private Boolean isAdjustKernelParam;

    /**
     * execStatus（0：not_run；1：full_run；2：full_finish；3：incremental_run；4：reverse_run; 5: fail; 6: finish 1000: wait_resource）
     */
    private Integer execStatus;
    private Integer mainTaskId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date execTime;


    @TableField(exist = false)
    private String taskLog;

    @TableField(exist = false)
    private List<MigrationTaskParam> taskParams;

    private String statusDesc;

    @TableField(exist = false)
    private Integer checkDataLevelingAndIncrementFinish;

    @TableField(exist = false)
    private Boolean isSystemAdmin;

    @Tolerate
    public MigrationTask(){

    }

}
