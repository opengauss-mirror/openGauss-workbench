/*
 * Copyright (c)Huawei Technologies Co.,Ltd 2025.
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
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ParamModel
 *
 * @since 2024-07-01
 */
@Data
@NoArgsConstructor
@TableName("tb_transcribe_replay_param")
public class ParamModel {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private int failSqlId;
    private String type;
    private String value;

    /**
     * ParamModel instructor from db
     *
     * @param rs resultSet
     * @throws SQLException SQLException
     */
    public ParamModel(ResultSet rs) throws SQLException {
        this.id = rs.getInt("para_index");
        this.type = rs.getString("para_type").trim();
        this.value = rs.getString("para_value");
    }

    /**
     * ParamModel instructor from json
     *
     * @param paramStr paramStr
     * @param index index
     */
    public ParamModel(String paramStr, int index) {
        if ("{}".equals(paramStr)) {
            this.id = index + 1;
            this.type = "null";
        }
        int splitIndex = paramStr.indexOf(":");
        if (splitIndex != -1) {
            this.id = index + 1;
            this.type = paramStr.substring(2, splitIndex - 1);
            this.value = paramStr.substring(splitIndex + 2, paramStr.length() - 2);
        }
    }
}
