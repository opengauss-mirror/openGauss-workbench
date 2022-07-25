package com.nctigba.datastudio.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Debug transmission entity class
 */
@Data
@NoArgsConstructor
public class PublicParamReq {
    // operate type
    private String operation;

    // function/procedure sql
    private String sql;

    // web user
    private String webUser;

    // connection name
    private String connectionName;

    // break point list
    private List<Integer> breakPoints;

    // input param list
    private List<Map<String, Object>> inputParams;

    // break point line
    private Integer line;

    // function/procedure name(include param)
    private String fullName;

    // function/procedure schema
    private String schema;

    // is debug
    private boolean isDebug;

    // window name
    private String windowName;

    @Override
    public String toString() {
        return "PublicParamReq{" +
                "operation='" + operation + '\'' +
                ", sql='" + sql + '\'' +
                ", webUser='" + webUser + '\'' +
                ", connectionName='" + connectionName + '\'' +
                ", breakPoints=" + breakPoints +
                ", inputParams=" + inputParams +
                ", line=" + line +
                ", fullName='" + fullName + '\'' +
                ", schema='" + schema + '\'' +
                ", isDebug=" + isDebug +
                ", windowName='" + windowName + '\'' +
                '}';
    }
}
