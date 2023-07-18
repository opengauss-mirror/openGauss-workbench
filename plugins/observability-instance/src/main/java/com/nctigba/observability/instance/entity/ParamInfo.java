/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "param_info", autoResultMap = true)
@NoArgsConstructor
public class ParamInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("paramType")
    type paramType;
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

    public enum type {
        OS,
        DB
    }

    public static List<ParamInfo> parse(ResultSet rs) throws SQLException {
        var list = new ArrayList<ParamInfo>();
        while (rs.next()) {
            var info = new ParamInfo();
            info.setId(rs.getInt("id"));
            info.setParamName(rs.getString("paramName"));
            info.setParamType(type.valueOf(rs.getString("paramType")));
            info.setSuggestValue(rs.getString("suggestValue"));
            info.setDefaultValue(rs.getString("defaultValue"));
            list.add(info);
        }
        return list;
    }
}