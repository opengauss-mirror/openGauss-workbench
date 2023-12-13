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
 *  EnvironmentServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/EnvironmentServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.service.EnvironmentService;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * EnvironmentServiceImpl
 *
 * @since 2023/12/8 15:35
 */
@Service
public class EnvironmentServiceImpl implements EnvironmentService {
    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;

    /**
     * get clusters
     *
     * @return List
     */
    public List cluster() {
        return opsFacade.listCluster();
    }

    /**
     * check Prometheus is exist
     */
    public void checkPrometheus() {
        List<NctigbaEnvDO> env = envMapper
            .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.Type.PROMETHEUS));
        if (CollectionUtil.isEmpty(env)) {
            throw new ServiceException("the promethues is uninstall");
        }
    }
}
