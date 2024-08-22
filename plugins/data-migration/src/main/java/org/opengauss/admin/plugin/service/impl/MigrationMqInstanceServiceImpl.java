/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.mapper.MigrationThirdPartySoftwareInstanceMapper;
import org.opengauss.admin.plugin.service.MigrationMqInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MigrationMqInstanceServiceImpl
 *
 * @author: www
 * @date: 2023/11/28 16:02
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@Service
@Slf4j
public class MigrationMqInstanceServiceImpl extends ServiceImpl<MigrationThirdPartySoftwareInstanceMapper,
        MigrationThirdPartySoftwareConfig> implements MigrationMqInstanceService {
    @Autowired
    MigrationThirdPartySoftwareInstanceMapper migrationMQInstanceMapper;

    /**
     * selectMQPage
     *
     * @author: www
     * @date: 2023/11/28 15:36
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param page page
     * @param config config
     * @return IPage<MigrationThirdPartySoftwareConfig>
     */
    @Override
    public IPage<MigrationThirdPartySoftwareConfig> selectMQPage(IPage<MigrationThirdPartySoftwareConfig> page,
                                                                 MigrationThirdPartySoftwareConfig config) {
        return migrationMQInstanceMapper.selectMQPage(page, config);
    }

    /**
     * listBindHostsByPortalHost
     *
     * @author: www
     * @date: 2023/11/28 15:37
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param host host
     * @return List<String>
     */
    @Override
    public List<String> listBindHostsByPortalHost(String host) {
        LambdaQueryWrapper<MigrationThirdPartySoftwareConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MigrationThirdPartySoftwareConfig::getKafkaIp, host)
                .isNotNull(MigrationThirdPartySoftwareConfig::getBindPortalHost);
        MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig = getOne(wrapper);
        if (thirdPartySoftwareConfig == null) {
            return null;
        }
        String bindPortalHost = thirdPartySoftwareConfig.getBindPortalHost();
        List<String> bindPortals = null;
        if (!StringUtils.isEmpty(bindPortalHost)) {
            bindPortals = Arrays.asList(bindPortalHost.split(","));
        }
        return bindPortals;
    }

    /**
     * removeInstance
     *
     * @author: www
     * @date: 2023/11/28 15:37
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param host host
     * @return String
     */
    @Override
    public String removeInstance(String host) {
        try {
            LambdaQueryWrapper<MigrationThirdPartySoftwareConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(MigrationThirdPartySoftwareConfig::getBindPortalHost, host);
            MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig = getOne(wrapper);
            if (thirdPartySoftwareConfig == null) {
                // 没有绑定其他的kafka 需要删除自己部署的kafka
                wrapper.clear();
                wrapper.eq(MigrationThirdPartySoftwareConfig::getKafkaIp, host);
                List<MigrationThirdPartySoftwareConfig> removeSoftWareRecord = list(wrapper);
                remove(wrapper);
                if (ObjectUtils.isEmpty(removeSoftWareRecord)) {
                    log.info("remove soft ware record is null");
                    return null;
                }
                return removeSoftWareRecord.get(0).getInstallDir();
            } else {
                // 绑定了其他的kafka 在绑定列表里删除
                String bindPortalHost = thirdPartySoftwareConfig.getBindPortalHost();
                ArrayList<String> bindPortals = new ArrayList<>(Arrays.asList(bindPortalHost.split(",")));
                bindPortals.remove(host);
                thirdPartySoftwareConfig.setBindPortalHost(StringUtils.join(bindPortals, ","));
                saveOrUpdate(thirdPartySoftwareConfig);
                return null;
            }
        } catch (MybatisPlusException e) {
            log.error("MybatisPlusException :", e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecord(MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig) {
        MigrationThirdPartySoftwareConfig newThirdPartySoftwareConfig = new MigrationThirdPartySoftwareConfig();
        BeanUtil.copyProperties(thirdPartySoftwareConfig, newThirdPartySoftwareConfig);
        MigrationThirdPartySoftwareConfig old = getOneByKafkaIp(thirdPartySoftwareConfig.getKafkaIp());
        if (old != null) {
            newThirdPartySoftwareConfig.setId(old.getId());
        }
        saveOrUpdate(newThirdPartySoftwareConfig);
    }

    @Override
    public MigrationThirdPartySoftwareConfig getOneByKafkaIp(String kafkaIp) {
        LambdaQueryWrapper<MigrationThirdPartySoftwareConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationThirdPartySoftwareConfig::getKafkaIp, kafkaIp).last("limit 1");
        return getOne(queryWrapper);
    }
}
