/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  IServerInfoService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/IServerInfoService.java
 *
 *  -------------------------------------------------------------------------
 */
package com.nctigba.observability.instance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.observability.instance.model.query.ServerInfoQuery;
import com.nctigba.observability.instance.model.entity.ServerInfoDO;

/**
 * <p>
 * Service
 * </p>
 *
 * @author liudm@vastdata.com.cn
 * @since 2022-09-05
 */
public interface IServerInfoService extends IService<ServerInfoDO> {
    /**
     * Test server connectivity
     *
     * @param info server information
     */
    void connectAvailable(ServerInfoQuery info);

}
