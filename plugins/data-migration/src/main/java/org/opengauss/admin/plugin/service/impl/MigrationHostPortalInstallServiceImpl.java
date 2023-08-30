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

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.mapper.MigrationHostPortalInstallMapper;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationHostPortalInstallServiceImpl extends ServiceImpl<MigrationHostPortalInstallMapper, MigrationHostPortalInstall> implements MigrationHostPortalInstallHostService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecord(MigrationHostPortalInstall install) {
        MigrationHostPortalInstall newInstall = new MigrationHostPortalInstall();
        BeanUtil.copyProperties(install, newInstall);
        MigrationHostPortalInstall pi = getOneByHostId(install.getRunHostId());
        if (pi != null) {
            newInstall.setId(pi.getId());
        }
        saveOrUpdate(newInstall);
    }

    @Override
    public void updateStatus(String hostId, Integer status) {
        MigrationHostPortalInstall pi = getOneByHostId(hostId);
        pi.setRunHostId(hostId);
        pi.setInstallStatus(status);
        updateById(pi);
    }

    @Override
    public MigrationHostPortalInstall getOneByHostId(String hostId) {
        LambdaQueryWrapper<MigrationHostPortalInstall> query = new LambdaQueryWrapper<>();
        query.eq(MigrationHostPortalInstall::getRunHostId, hostId).last("limit 1");
        return getOne(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearPkgUploadPath(String hostId) {
        MigrationHostPortalInstall pi = getOneByHostId(hostId);
        if (pi != null) {
            pi.setRunHostId(hostId);
            pi.setPkgUploadPath(new UploadInfo());
            updateById(pi);
        }
    }
}
