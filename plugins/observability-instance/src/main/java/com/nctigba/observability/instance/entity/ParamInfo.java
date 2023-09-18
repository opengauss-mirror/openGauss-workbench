/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class ParamInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("paramType")
    ParamType paramType;
    @TableField("paramName")
    String paramName;
    String paramDetail;
    @TableField("suggestValue")
    String suggestValue;
    @TableField("defaultValue")
    String defaultValue;
    String unit;
    String suggestExplain;
    @TableField("diagnosisRule")
    String diagnosisRule;
    String parameterCategory;
    String valueRange;

    public enum ParamType {
        OS,
        DB,
        CLUSTER
    }

    public static List<ParamInfo> parse(ResultSet rs) throws SQLException {
        var list = new ArrayList<ParamInfo>();
        while (rs.next()) {
            var info = new ParamInfo();
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