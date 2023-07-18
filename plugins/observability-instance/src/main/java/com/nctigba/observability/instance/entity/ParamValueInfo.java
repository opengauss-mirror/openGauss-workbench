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
@TableName(value = "param_value_info", autoResultMap = true)
@NoArgsConstructor
public class ParamValueInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("sid")
    Integer sid;
    @TableField("instance")
    String instance;
    @TableField("actualValue")
    String actualValue;

    public static List<ParamValueInfo> parse(ResultSet rs) throws SQLException {
        var list = new ArrayList<ParamValueInfo>();
        while (rs.next()) {
            var value = new ParamValueInfo();
            value.setId(rs.getInt(1));
            value.setSid(rs.getInt(2));
            value.setInstance(rs.getString(3));
            value.setActualValue(rs.getString(4));
            list.add(value);
        }
        return list;
    }

    public ParamValueInfo(Integer sid, String instance, String actualValue) {
        super();
        this.sid = sid;
        this.instance = instance;
        this.actualValue = actualValue;
    }
}
