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
 * SysTask.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/domain/SysTask.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Task Model
 *
 * @author xielibo
 */
@TableName(value ="sys_task")
@Data
public class SysTask {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * task name
     */
    private String taskName;

    /**
     * task type
     */
    private Integer taskType;

    /**
     * exec status
     */
    private Integer execStatus;

    /**
     * exec params
     */
    private String execParams;

    /**
     * exec progress
     */
    private Float execProgress;

    /**
     * exec hostId
     */
    private String execHostId;

    /**
     * plugin id
     */
    private String pluginId;

    private Integer subTaskCount;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * finish time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /**
     * execTime time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date execTime;
}
