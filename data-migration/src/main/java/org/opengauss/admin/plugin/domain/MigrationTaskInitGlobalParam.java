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
 * MigrationTaskInitGlobalParam.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/domain/MigrationTaskInitGlobalParam.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * MigrationTaskInitGlobalParam Object
 *
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Data
@TableName("tb_migration_task_init_global_param")
public class MigrationTaskInitGlobalParam {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * field paramKey
     */
    private String paramKey;

    /**
     * field paramValue
     */
    private String paramValue;

    /**
     * field paramDesc
     */
    private String paramDesc;

    /**
     * field paramType
     */
    private Integer paramType;

    /**
     * field paramExtends
     */
    private String paramExtends;

    /**
     * field paramRules
     */
    private String paramRules;
}
