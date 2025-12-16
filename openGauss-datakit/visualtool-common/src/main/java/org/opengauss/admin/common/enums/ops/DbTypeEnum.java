/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * DbTypeEnum.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/ops/DbTypeEnum.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums.ops;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lhf
 * @date 2023/1/13 11:00
 **/
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("com.mysql.cj.jdbc.Driver"),
    OPENGAUSS("org.opengauss.Driver"),
    POSTGRESQL("org.postgresql.Driver"),
    MILVUS(null),
    ELASTICSEARCH(null),
    ;

    private String driverClass;

    /**
     * Get driver class
     *
     * @return driver class
     */
    public String getDriverClass() {
        if (StrUtil.isEmpty(driverClass)) {
            throw new UnsupportedOperationException("DbTypeEnum " + name() + " does not have driver class");
        }
        return driverClass;
    }

    /**
     * Whether it is a JDBC driver database type
     *
     * @return boolean
     */
    public boolean isJdbcDriver() {
        return driverClass != null;
    }

    public static DbTypeEnum typeOf(String dbType) {
        if (StrUtil.isEmpty(dbType)) {
            return null;
        }

        for (DbTypeEnum enumConstant : DbTypeEnum.class.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(dbType)) {
                return enumConstant;
            }
        }
        return null;
    }
}
