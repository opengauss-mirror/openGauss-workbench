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

import java.util.Locale;

/**
 * @author lhf
 * @date 2023/1/13 11:00
 **/
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%d/%s", "mysql"),
    OPENGAUSS("org.opengauss.Driver", "jdbc:opengauss://%s:%d/%s", "postgres"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s", "postgres"),
    MILVUS(null, null, null),
    ELASTICSEARCH(null, null, null),
    ;

    private String driverClass;
    private String jdbcUrlFormat;
    private String defaultConnectDb;

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

    /**
     * Generate JDBC URL
     *
     * @param ip host ip
     * @param port database port
     * @return JDBC URL
     */
    public String generateJdbcUrl(String ip, Integer port) {
        if (isJdbcDriver()) {
            return String.format(Locale.ROOT, jdbcUrlFormat, ip, port, defaultConnectDb);
        }
        throw new UnsupportedOperationException("DbTypeEnum " + name() + " does not support generate jdbc url");
    }
}
