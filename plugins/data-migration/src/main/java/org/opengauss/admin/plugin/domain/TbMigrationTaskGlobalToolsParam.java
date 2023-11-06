/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.enums.TaskParamType;

import java.util.List;

/**
 * TbMigrationTaskGlobalToolsParam
 *
 * @author: www
 * @date: 2023/11/28 15:13
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("tb_migration_task_global_tools_param")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ToString
public class TbMigrationTaskGlobalToolsParam {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("param_key")
    private String paramKey;

    @TableField("param_value")
    private String paramValue;

    @TableField("param_change_value")
    private String paramChangeValue;

    @TableField("param_value_type")
    private Integer paramValueType;

    @TableField("config_id")
    private Integer configId;

    @TableField("portal_host_id")
    private String portalHostID;

    //  0 不是新增参数 1是新增参数
    @TableField("param_desc")
    private String paramDesc;

    // 0 存在 1 删除
    @TableField("delete_flag")
    private Integer deleteFlag;

    @TableField("new_param_flag")
    private Integer newParamFlag;

    /**
     * 设置paramValue paramValueType
     *
     * @author: www
     * @date: 2023/11/28 15:15
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param value value
     */
    public void setParamValueAndType(Object value) {
        if (value instanceof List) {
            this.paramValue = String.join(",", (List<String>) value);
            this.paramValueType = TaskParamType.TYPE_LIST.getCode();
        } else if (value instanceof Integer) {
            this.paramValue = value.toString();
            this.paramValueType = TaskParamType.TYPE_NUMBER.getCode();
        } else if (value instanceof Boolean) {
            this.paramValue = value.toString();
            this.paramValueType = TaskParamType.TYPE_BOOLEAN.getCode();
        } else {
            this.paramValue = value.toString();
            this.paramValueType = TaskParamType.TYPE_STRING.getCode();
        }
    }

    /**
     * DeleteFlagEnum
     *
     * @author: www
     * @date: 2023/11/28 15:16
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    @AllArgsConstructor
    @Getter
    public enum DeleteFlagEnum {
        DELETE(1, "已删除"),
        USED(0, "使用中");
        private Integer deleteFlag;
        private String deleteFlagDesc;
    }


    /**
     * NewParamFlagEnum
     *
     * @author: www
     * @date: 2023/11/28 15:16
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    @AllArgsConstructor
    @Getter
    public enum NewParamFlagEnum {
        NEW_PARAM(1, "是新增参数"),
        OLD_PARAM(0, "不是新增参数");
        private Integer newParamFlag;
        private String newParamFlagDesc;
    }
}
