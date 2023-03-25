package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.ShardingDatasourceConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OlkParamDto;

import java.util.List;

@Data
@TableName("ops_olk")
public class OpsOlkEntity extends BaseEntity {
    @TableId
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
    @TableField(exist = false)
    private String ssInstallPassword;
    @TableField(exist = false)
    private String olkInstallPassword;
    private String tableName;
    private String columns;
    private String ruleYaml;
}
