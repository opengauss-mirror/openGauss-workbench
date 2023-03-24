package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;

@Data
public class OlkPageVO {
    private String id;
    private String name;
    private String dadPkgName;
    private String dadInstallIp;
    private String dadInstallUsername;
    private String dadInstallPath;
    private String dadPort;
    private String zkPkgName;
    private String zkPort;
    private String ssPkgName;
    private String ssInstallIp;
    private String ssInstallUsername;
    private String ssInstallPath;
    private String ssUploadPath;
    private String ssPort;
    private String olkPkgName;
    private String olkInstallIp;
    private String olkInstallUsername;
    private String olkInstallPath;
    private String olkUploadPath;
    private String olkPort;
    private String tableName;
    private String columns;
    private String ruleYaml;
    private String remark;
    private String updateTime;
}
