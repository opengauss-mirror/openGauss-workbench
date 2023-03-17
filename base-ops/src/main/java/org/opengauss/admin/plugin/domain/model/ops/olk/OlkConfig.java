package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadReqPath;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OlkParamDto;

import java.nio.file.Path;
import java.util.List;

@Data
public class OlkConfig {
    private String id;
    private String name;
    private String olkTarId;
    private String ssTarId;
    private String dadTarId;
    private String dadPort;
    private String dadInstallPath;
    private String dadInstallHostId;
    private String dadInstallUsername;
    private String dadInstallPassword;
    private String zkTarId;
    private String ssPort;
    private String olkPort;
    private String zkPort;
    private String ssInstallPath;
    private String ssUploadPath;
    private String olkInstallPath;
    private String olkUploadPath;
    private String ssInstallHostId;
    private String olkInstallHostId;
    private String ssInstallUsername;
    private String olkInstallUsername;
    private String ssInstallPassword;
    private String olkInstallPassword;
    private OlkParamDto olkParamConfig;
    private String remark;
    // sharding datasource config
    private List<ShardingDatasourceConfig> dsConfig;
    private String tableName;
    private String column;
    private String ruleYaml;

    public String getDadLogFileName () {
        // return Path.of(dadInstallPath, DadReqPath.LOG_FILE_NAME + id).toString();
        return Path.of(dadInstallPath, DadReqPath.OUTPUT_LOG).toString().replace("\\", "/");
    }
}
