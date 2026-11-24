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

package com.nctigba.observability.sql.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.observability.sql.enums.GrabTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ResourceDO
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@TableName(value = "diagnosis_resource", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class ResourceDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer taskid;
    @TableField("grabType")
    GrabTypeEnum grabType;
    String f;

    /**
     * Construction method
     *
     * @param task     Diagnosis task info
     * @param grabType Bcc type
     */
    public ResourceDO(DiagnosisTaskDO task, GrabTypeEnum grabType) {
        super();
        this.taskid = task.getId();
        this.grabType = grabType;
    }
}