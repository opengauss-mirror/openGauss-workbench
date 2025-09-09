/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.SysSettingExtEntity;

import java.util.List;

/**
 * ISysSettingExtService
 *
 * @author: wangchao
 * @Date: 2025/9/8 15:47
 * @Description: ISysSettingExtService
 * @since 7.0.0
 **/
public interface ISysSettingExtService extends IService<SysSettingExtEntity> {
    /**
     * query current user ext param
     *
     * @param userId userId
     * @return ext param list
     */
    List<SysSettingExtEntity> queryCurrentUserExtParam(Integer userId);

    /**
     * update ext setting of datakit server host
     *
     * @param setting setting
     */
    void updateExtSettingDataKitServerHost(SysSettingEntity setting);

    /**
     * query admin user of datakit server host
     *
     * @return default server host
     */
    String queryAdminServerHost();
}
