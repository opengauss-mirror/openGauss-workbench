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
 *  PublicParamQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/PublicParamQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.query;

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
public class PublicParamQuery {
    private String operation;

    private String sql;

    private String webUser;

    private List<Integer> breakPoints;

    private List<Map<String, Object>> inputParams;

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

    private Integer pageNum;

    private Integer pageSize;

    private String resultId;

    @Override
    public String toString() {
        return "PublicParamReq{"
                + "operation='" + operation + '\''
                + ", sql='" + sql + '\''
                + ", webUser='" + webUser + '\''
                + ", breakPoints=" + breakPoints
                + ", inputParams=" + inputParams
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
                + ", pageNum='" + pageNum + '\''
                + ", pageSize='" + pageSize + '\''
                + ", resultId='" + resultId + '\''
                + '}';
    }
}
