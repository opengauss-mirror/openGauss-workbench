package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ops_olk")
public class OpsOlkEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String olkTarId;
    private String ssTarId;
    private String dadTarId;
    private String zkTarId;
    private String ssPort;
    private String olkPort;
    private String zkPort;
    private String ssInstallPath;
    private String olkInstallPath;
    private String ssInstallHostId;
    private Integer olkInstallHostId;
    private String ssInstallUserName;
    private String olkInstallUserName;
    private String config;
    private String remark;
}
