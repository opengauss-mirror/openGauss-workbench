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

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;

import java.util.List;

/**
 * MigrationMqInstanceService
 *
 * @author: www
 * @date: 2023/11/28 15:33
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
public interface MigrationMqInstanceService extends IService<MigrationThirdPartySoftwareConfig> {
    /**
     * selectMQPage
     *
     * @author: www
     * @date: 2023/11/28 15:31
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param page  page
     * @param config  config
     * @return IPage<MigrationThirdPartySoftwareConfig>
     */
    IPage<MigrationThirdPartySoftwareConfig> selectMQPage(IPage<MigrationThirdPartySoftwareConfig> page,
                                                          MigrationThirdPartySoftwareConfig config);

    /**
     * listBindHostsByPortalHost
     *
     * @author: www
     * @date: 2023/11/28 15:31
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param host host
     * @return List<String>
     */
    List<String> listBindHostsByPortalHost(String host);

    /**
     * removeInstance
     *
     * @author: www
     * @date: 2023/11/28 15:31
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param host host
     * @return String
     */
    String removeInstance(String host);

    /**
     * save third party software config
     *
     * @param thirdPartySoftwareConfig third party software config
     */
    void saveRecord(MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig);

    /**
     * get one third party software config by kafka ip
     *
     * @param kafkaIp kafka ip
     * @return MigrationThirdPartySoftwareConfig
     */
    MigrationThirdPartySoftwareConfig getOneByKafkaIp(String kafkaIp);
}
