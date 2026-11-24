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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ParamInfoDO
 *
 * @author luomeng
 * @since 2023/9/26
 */
@Data
@Accessors(chain = true)
@TableName(value = "param_info", autoResultMap = true)
@NoArgsConstructor
public class ParamInfoDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("paramType")
    ParamType paramType;
    @TableField("paramName")
    String paramName;
    @TableField("paramDetail")
    String paramDetail;
    @TableField("suggestValue")
    String suggestValue;
    @TableField("defaultValue")
    String defaultValue;
    String unit;
    @TableField("suggestExplain")
    String suggestExplain;
    @TableField("diagnosisRule")
    String diagnosisRule;

    /**
     * ParamType
     *
     * @author luomeng
     * @since 2023/9/26
     */
    public enum ParamType {
        OS,
        DB
    }
}