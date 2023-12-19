/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ParamValueInfoDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/entity/ParamValueInfoDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName(value = "param_value_info", autoResultMap = true)
@NoArgsConstructor
public class ParamValueInfoDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer sid;
    String instance;
    String actualValue;

    public static List<ParamValueInfoDO> parse(ResultSet rs) throws SQLException {
        var list = new ArrayList<ParamValueInfoDO>();
        while (rs.next()) {
            var value = new ParamValueInfoDO();
            value.setId(rs.getInt(1));
            value.setSid(rs.getInt(2));
            value.setInstance(rs.getString(3));
            value.setActualValue(rs.getString(4));
            list.add(value);
        }
        return list;
    }

    public ParamValueInfoDO(Integer sid, String instance, String actualValue) {
        super();
        this.sid = sid;
        this.instance = instance;
        this.actualValue = actualValue;
    }
}
