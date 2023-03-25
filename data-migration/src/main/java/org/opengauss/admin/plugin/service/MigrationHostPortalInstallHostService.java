package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationHostPortalInstallHostService extends IService<MigrationHostPortalInstall> {


    void saveRecord(String hostId,String hostUserId,  String host, Integer port, String user, String password, String installPath, Integer status);

    void updateStatus(String hostId, Integer status);

    MigrationHostPortalInstall getOneByHostId(String hostId);
}
