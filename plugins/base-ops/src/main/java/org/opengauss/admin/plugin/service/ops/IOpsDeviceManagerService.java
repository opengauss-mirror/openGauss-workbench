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

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDeviceManagerEntity;

import java.util.List;

/**
 * device manager service interface
 *
 * @author wbd
 * @since 2024/1/23 17:37
 **/
public interface IOpsDeviceManagerService extends IService<OpsDeviceManagerEntity> {
    /**
     * query repeat device manager name
     *
     * @param name name
     * @return boolean
     */
    boolean hasName(String name);

    /**
     * list all device manager
     *
     * @return List
     */
    List<OpsDeviceManagerEntity> listDeviceManager();

    /**
     * add device manager
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return boolean
     */
    boolean add(OpsDeviceManagerEntity deviceManagerEntity);

    /**
     * modify device manager
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return boolean
     */
    boolean modify(OpsDeviceManagerEntity deviceManagerEntity);

    /**
     * delete device manager
     *
     * @param clusterName clusterName
     * @return boolean
     */
    boolean delete(String clusterName);

    /**
     * connect device manager
     *
     * @param deviceManagerEntity deviceManagerEntity
     * @return boolean
     */
    boolean connect(OpsDeviceManagerEntity deviceManagerEntity);
}
