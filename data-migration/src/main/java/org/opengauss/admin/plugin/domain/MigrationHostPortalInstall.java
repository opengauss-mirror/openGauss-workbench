package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Host portal install model
 *
 * @author xielibo
 */
@TableName(value ="tb_migration_host_portal_install")
@Data
public class MigrationHostPortalInstall {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String runHostId;

    // 0 ：not install  1：installing；2：Installed；10：install error
    private Integer installStatus;

    private String installPath;

    private String hostUserId;

    private String host;
    private Integer port;
    private String runUser;
    private String runPassword;
}
