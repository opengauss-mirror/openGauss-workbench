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
 * MigrationHostPortalInstallServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationHostPortalInstallServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


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
