/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.jcraft.jsch.Session;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.service.LinuxConfigService;
import org.opengauss.collect.utils.IdUtils;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.TimeOutUtil;
import org.opengauss.collect.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * LinuxConfigServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class LinuxConfigServiceImpl implements LinuxConfigService {
    @Autowired
    private Cache<Long, LinuxConfig> linuxConfigCache;

    @Override
    public RespBean getLinuxConfigList(LinuxConfig config) {
        List<LinuxConfig> list = linuxConfigCache.asMap().values().stream().collect(Collectors.toList());
        return RespBean.success("success", list);
    }

    @Override
    public RespBean saveLinuxConfig(LinuxConfig config) {
        String host = config.getHost();
        List<LinuxConfig> list = linuxConfigCache.asMap().values().stream().collect(Collectors.toList());
        boolean isContainsHost = list.stream()
                .anyMatch(item -> item.getHost().contains(host));
        if (isContainsHost) {
            return RespBean.error("Host already exists");
        }
        Session session = executeWithTimeout(config);
        if (ObjectUtil.isEmpty(session)) {
            return RespBean.error("Failed to save Linux connection information");
        }
        Long id = IdUtils.SNOWFLAKE.nextId();
        config.setConfigId(id);
        linuxConfigCache.put(id, config);
        JschUtil.closeSession(session);
        return RespBean.success("save success");
    }

    @Override
    public RespBean updateLinuxConfig(LinuxConfig config) {
        String host = config.getHost();
        LinuxConfig result = linuxConfigCache.asMap().entrySet().stream()
                .filter(entry -> entry.getValue().getHost().equals(host))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (result != null && !result.getConfigId().equals(config.getConfigId())) {
            return RespBean.error("Host already exists");
        }
        Session session = executeWithTimeout(config);
        if (ObjectUtil.isEmpty(session)) {
            return RespBean.error("Failed to save Linux connection information");
        }
        JschUtil.closeSession(session);
        linuxConfigCache.invalidate(config.getConfigId());
        linuxConfigCache.put(config.getConfigId(), config);
        return RespBean.success("update success");
    }

    private Session executeWithTimeout(LinuxConfig config) {
        Callable<Session> task = () -> JschUtil.obtainSession(config);
        try {
            return TimeOutUtil.executeWithTimeout(task, 2, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            throw new ServiceException("connection to linux timeout");
        }
    }

    @Override
    public RespBean deleteLinuxConfig(List<Integer> ids) {
        ids.forEach(linuxConfigCache::invalidate);
        return RespBean.success("delete success");
    }

    @Override
    public RespBean getHostList() {
        List<String> list = linuxConfigCache.asMap().values().stream().map(LinuxConfig::getHost)
                .collect(Collectors.toList());
        return RespBean.success("success", list);
    }
}
