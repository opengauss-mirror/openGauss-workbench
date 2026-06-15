/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.opengauss.third.party.tools.csv.CsvExportable;

import java.util.Locale;

/**
 * Base install info class
 *
 * @since 2026/6/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseInstallInfo implements CsvExportable {
    private String id;
    private String ip;
    private Integer port;
    private String user;
    private String installDir;

    @Override
    public String toString() {
        return "id='" + id + '\''
                + ", ip='" + ip + '\''
                + ", port=" + port
                + ", user='" + user + '\''
                + ", installDir='" + installDir + '\'';
    }

    @Override
    public String getCsvHeader() {
        return "id,ip,port,user,install_dir";
    }

    @Override
    public String getCsvData() {
        checkIllegalCharacter(id);
        checkIllegalCharacter(ip);
        checkIllegalCharacter(user);
        checkIllegalCharacter(installDir);
        if (port == null) {
            throw new IllegalArgumentException("port must not be null");
        }
        return String.format(Locale.ROOT, "%s,%s,%d,%s,%s", id, ip, port, user, installDir);
    }

    /**
     * Check illegal character in field
     *
     * @param field field value
     */
    protected void checkIllegalCharacter(String field) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be null or empty");
        }
        if (field.equalsIgnoreCase("null")) {
            throw new IllegalArgumentException("field value must not be 'null'");
        }
        if (field.contains(",")) {
            throw new IllegalArgumentException("field value must not contain ','");
        }
        if (field.contains("\n")) {
            throw new IllegalArgumentException("field value must not contain '\\n'");
        }
        if (field.contains("\r")) {
            throw new IllegalArgumentException("field value must not contain '\\r'");
        }
    }
}
