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
 * MainTaskEnvErrorHostServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MainTaskEnvErrorHostServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MainTaskEnvErrorHost;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.mapper.MainTaskEnvErrorHostMapper;
import org.opengauss.admin.plugin.service.MainTaskEnvErrorHostService;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MainTaskEnvErrorHostServiceImpl extends ServiceImpl<MainTaskEnvErrorHostMapper, MainTaskEnvErrorHost> implements MainTaskEnvErrorHostService {


    @Override
    public void saveRecord(Integer mainTaskId, MigrationTaskHostRef hostRef) {
        MainTaskEnvErrorHost host = new MainTaskEnvErrorHost();
        host.setMainTaskId(mainTaskId);
        host.setRunHostId(hostRef.getRunHostId());
        host.setRunHost(hostRef.getHost());
        host.setRunPort(hostRef.getPort());
        host.setRunUser(hostRef.getUser());
        host.setRunPass(hostRef.getPassword());
        this.save(host);
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MainTaskEnvErrorHost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MainTaskEnvErrorHost::getMainTaskId, mainTaskId);
        this.remove(queryWrapper);
    }

    @Override
    public MainTaskEnvErrorHost getOneByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MainTaskEnvErrorHost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MainTaskEnvErrorHost::getMainTaskId, mainTaskId).last("limit 1");
        return this.getOne(queryWrapper);
    }

}
