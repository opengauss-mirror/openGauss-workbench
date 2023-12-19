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
 *  ParamInfoDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/entity/ParamInfoDO.java
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
@TableName(value = "param_info", autoResultMap = true)
@NoArgsConstructor
public class ParamInfoDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    ParamType paramType;
    String paramName;
    String paramDetail;
    String suggestValue;
    String defaultValue;
    String unit;
    String suggestExplain;
    String diagnosisRule;
    String parameterCategory;
    String valueRange;

    public enum ParamType {
        OS,
        DB,
        CLUSTER
    }

    public static List<ParamInfoDO> parse(ResultSet rs) throws SQLException {
        var list = new ArrayList<ParamInfoDO>();
        while (rs.next()) {
            var info = new ParamInfoDO();
            info.setId(rs.getInt("id"));
            info.setParamName(rs.getString("paramName"));
            info.setParamType(ParamType.valueOf(rs.getString("paramType")));
            info.setSuggestValue(rs.getString("suggestValue"));
            info.setDefaultValue(rs.getString("defaultValue"));
            info.setParameterCategory(rs.getString("parameterCategory"));
            info.setValueRange(rs.getString("valueRange"));
            info.setParamDetail(rs.getString("paramDetail"));
            info.setSuggestExplain(rs.getString("suggestExplain"));
            info.setUnit(rs.getString("unit"));
            info.setDiagnosisRule(rs.getString("diagnosisRule"));
            list.add(info);
        }
        return list;
    }
}