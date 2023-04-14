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
 * HostFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/HostFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author lhf
 * @date 2022/11/20 21:09
 **/
@Slf4j
@Service
public class HostFacade {

    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private OpsHostMapper hostMapper;
    @Autowired
    private IHostService hostService;

    public void add(HostBody hostBody) {
        hostService.add(hostBody);
    }

    public boolean ping(HostBody hostBody) {
        return hostService.ping(hostBody);
    }

    public boolean del(String hostId) {
        return hostService.del(hostId);
    }

    public boolean ping(String hostId, String rootPassword) {
        return hostService.ping(hostId, rootPassword);
    }

    public boolean edit(String hostId, HostBody hostBody) {
        return hostService.edit(hostId, hostBody);
    }

    public IPage<OpsHostVO> pageHost(Page page, String name) {
        return hostService.pageHost(page, name, null, null);
    }

    public OpsHostEntity getById(String id) {
        return hostService.getById(id);
    }

    public List<OpsHostEntity> listByIds(Collection ids) {
        return hostService.listByIds(ids);
    }

    public Long count() {
        return hostService.count();
    }

    public List<OpsHostEntity> listAll() {
        return hostService.listAll(null);
    }
}
