/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * PublicParamReq
 *
 * @since 2023-6-26
 */
@Data
@Generated
@NoArgsConstructor
public class PublicParamReq {
    private String operation;

    private String sql;

    private String webUser;

    private List<Integer> breakPoints;

    private List<Map<String, Object>> inputParams;

    private Integer line;

    private String fullName;

    private String schema;

    private String windowName;

    private String oldWindowName;

    private String rootWindowName;

    private boolean isCloseWindow;

    private String uuid;

    private String language;

    private String oid;

    private boolean isContinue;

    private boolean isCoverage;

    private boolean isInPackage;

    private boolean isPackage;

    @Override
    public String toString() {
        return "PublicParamReq{"
                + "operation='" + operation + '\''
                + ", sql='" + sql + '\''
                + ", webUser='" + webUser + '\''
                + ", breakPoints=" + breakPoints
                + ", inputParams=" + inputParams
                + ", line=" + line
                + ", fullName='" + fullName + '\''
                + ", schema='" + schema + '\''
                + ", windowName='" + windowName + '\''
                + ", oldWindowName='" + oldWindowName + '\''
                + ", rootWindowName='" + rootWindowName + '\''
                + ", isCloseWindow='" + isCloseWindow + '\''
                + ", uuid='" + uuid + '\''
                + ", language='" + language + '\''
                + ", oid='" + oid + '\''
                + ", isContinue='" + isContinue + '\''
                + ", isCoverage='" + isCoverage + '\''
                + ", isInPackage='" + isInPackage + '\''
                + ", isPackage='" + isPackage + '\''
                + '}';
    }
}
