/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.ops.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDeviceManagerEntity;
import org.opengauss.admin.plugin.mapper.ops.OpsDeviceManagerMapper;
import org.opengauss.admin.plugin.mapper.ops.OpsDisasterClusterMapper;
import org.opengauss.admin.plugin.service.ops.IOpsDeviceManagerService;
import org.opengauss.admin.plugin.utils.DeviceManagerUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Objects;

/**
 * device manager service implement
 *
 * @author wbd
 * @since 2024/1/24 11:38
 **/
@Slf4j
@Service
public class OpsDeviceManagerServiceImpl extends ServiceImpl<OpsDeviceManagerMapper, OpsDeviceManagerEntity> implements
    IOpsDeviceManagerService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    private OpsDisasterClusterMapper disasterClusterMapper;

    @Override
    public boolean hasName(String name) {
        OpsDeviceManagerEntity deviceManagerEntity = getById(name);
        return Objects.nonNull(deviceManagerEntity);
    }

    @Override
    public List<OpsDeviceManagerEntity> listDeviceManager() {
        return list();
    }

    @Override
    public boolean add(OpsDeviceManagerEntity deviceManagerEntity) {
        if (hasName(deviceManagerEntity.getName())) {
            throw new OpsException("the device manager name is exist");
        }
        return save(deviceManagerEntity);
    }

    @Override
    public boolean modify(OpsDeviceManagerEntity deviceManagerEntity) {
        // 直接调用update接口
        LambdaUpdateWrapper<OpsDeviceManagerEntity> updateWrapper = Wrappers.lambdaUpdate(OpsDeviceManagerEntity.class)
            .set(OpsDeviceManagerEntity::getHostIp, deviceManagerEntity.getHostIp())
            .set(OpsDeviceManagerEntity::getPort, deviceManagerEntity.getPort())
            .set(OpsDeviceManagerEntity::getUserName, deviceManagerEntity.getUserName())
            .set(OpsDeviceManagerEntity::getPassword, deviceManagerEntity.getPassword())
            .set(OpsDeviceManagerEntity::getPairId, deviceManagerEntity.getPairId())
            .eq(OpsDeviceManagerEntity::getName, deviceManagerEntity.getName());
        return update(updateWrapper);
    }

    @Override
    public boolean delete(String name) {
        // 先检查该磁阵是否被双集群关联，关联了则不能删除，抛出异常
        int size = disasterClusterMapper.queryDisasterClusterCountByDeviceManagerName(name);
        log.info("count is {}", size);
        if (size != 0) {
            throw new OpsException("the device manager is used by disaster cluster,can't delete");
        }
        LambdaQueryWrapper<OpsDeviceManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsDeviceManagerEntity.class)
            .eq(OpsDeviceManagerEntity::getName, name);
        return remove(queryWrapper);
    }

    @Override
    public boolean connect(OpsDeviceManagerEntity deviceManagerEntity) {
        // 需要调用API,根据响应结果判断是否能联通
        // 将密码解密后再测试联通性
        deviceManagerEntity.setPassword(encryptionUtils.decrypt(deviceManagerEntity.getPassword()));
        return !DeviceManagerUtil.authenticate(deviceManagerEntity).isEmpty();
    }
}
