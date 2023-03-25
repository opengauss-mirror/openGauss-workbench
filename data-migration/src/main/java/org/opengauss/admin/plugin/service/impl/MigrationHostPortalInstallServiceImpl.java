package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.mapper.MigrationHostPortalInstallMapper;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationHostPortalInstallServiceImpl extends ServiceImpl<MigrationHostPortalInstallMapper, MigrationHostPortalInstall> implements MigrationHostPortalInstallHostService {

    @Override
    public void saveRecord(String hostId, String hostUserId, String host, Integer port, String user, String password, String installPath, Integer status) {

        MigrationHostPortalInstall pi = getOneByHostId(hostId);
        if (pi == null) {
            pi = new MigrationHostPortalInstall();
        }
        pi.setRunHostId(hostId);
        pi.setHost(host);
        pi.setPort(port);
        pi.setRunUser(user);
        pi.setRunPassword(password);
        pi.setInstallPath(installPath);
        pi.setInstallStatus(status);
        pi.setHostUserId(hostUserId);
        this.saveOrUpdate(pi);
    }

    @Override
    public void updateStatus(String hostId, Integer status) {
        MigrationHostPortalInstall pi = getOneByHostId(hostId);
        pi.setRunHostId(hostId);
        pi.setInstallStatus(status);
        this.updateById(pi);
    }

    @Override
    public MigrationHostPortalInstall getOneByHostId(String hostId) {
        LambdaQueryWrapper<MigrationHostPortalInstall> query = new LambdaQueryWrapper<>();
        query.eq(MigrationHostPortalInstall::getRunHostId, hostId).last("limit 1");
        return this.getOne(query);
    }
}
