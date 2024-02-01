package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author duanguoqiang
 * @date 2024/1/31
 * @description PortalDownloadInfo Entity
 */
@Data
@TableName("tb_migration_tool_portal_download_info")
public class MigrationToolPortalDownloadInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String hostOs;
    private String hostOsVersion;
    private String hostCpuArch;
    private String portalPkgDownloadUrl;
    private String portalPkgName;
    private String portalJarName;
}
