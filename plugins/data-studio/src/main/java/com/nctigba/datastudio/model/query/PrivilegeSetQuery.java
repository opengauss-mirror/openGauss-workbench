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
 *  PrivilegeSetQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/PrivilegeSetQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PrivilegeSetQuery
 *
 * @author liupengfei
 * @since 2024/10/25
 */
@Data
@Generated
@NoArgsConstructor
public class PrivilegeSetQuery {
    private String uuid;
    private String grantOrRevoke;
    private List<PrivilegeOption> privilegeOption;
    private String type;
    private List<ObjValue> obj;
    private List<String> user;

    /**
     * PrivilegeOption
     */
    @Data
    public static class PrivilegeOption {
        private String privilege;
        private Boolean checkOption;
        private Boolean checkPrivilege;
    }

    /**
     * ObjValue
     */
    @Data
    public static class ObjValue {
        private String name;
        private String schema;
    }
}
