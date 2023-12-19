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
 *  ParamInfoServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/ParamInfoServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.instance.model.entity.ParamInfoDO;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ParamInfoService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.model.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.model.entity.ParamInfoDO.ParamType;
import com.nctigba.observability.instance.model.entity.ParamValueInfoDO;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.mapper.ParamInfoMapper;
import com.nctigba.observability.instance.mapper.ParamValueInfoMapper;
import com.nctigba.observability.instance.model.query.ParamQuery;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import com.nctigba.observability.instance.util.SshSessionUtils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ParamInfoServiceImpl
 *
 * @since 2023/12/3 10:26
 */
@Slf4j
@Service
public class ParamInfoServiceImpl extends ServiceImpl<ParamInfoMapper, ParamInfoDO>
        implements ParamInfoService, InitializingBean {
    private static final List<ParamInfoDO> LIST = new ArrayList<>();
    private static final Map<Integer, ParamInfoDO> IDS = new HashMap<>();
    private static final Map<ParamInfoDO.ParamType, Map<String, ParamInfoDO>> MAP = new HashMap<>();

    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private ParamValueInfoMapper paramValueInfoMapper;
    @Autowired
    private DbConfigMapper dbConfigMapper;
    @Autowired(required = false)
    private ParamValueInfoServiceImpl paramValueInfoService;

    /**
     * getParamInfo
     *
     * @param paramQuery ParamQuery
     * @return List<ParamInfoDTO>
     */
    public List<ParamInfoDTO> getParamInfo(ParamQuery paramQuery) {
        if ("1".equals(paramQuery.getIsRefresh())) {
            if (StrUtil.isBlank(paramQuery.getPassword())) {
                return new ArrayList<>();
            }
            this.updateOsParamInfo(paramQuery);
            this.updateDatabaseParamInfo(paramQuery);
            paramValueInfoService.refresh(paramQuery.getNodeId());
        }
        return paramValueInfoService.query(paramQuery.getNodeId());
    }

    private void updateDatabaseParamInfo(ParamQuery paramQuery) {
        try {
            clusterManager.setCurrentDatasource(paramQuery.getNodeId(), null);
            var list = new ArrayList<ParamValueInfoDO>();
            for (Map<String, String> setting : dbConfigMapper.settings()) {
                var paramInfo = getParamInfo(ParamType.DB, setting.get("name"));
                if (paramInfo == null) {
                    continue;
                }
                list.add(new ParamValueInfoDO(paramInfo.getId(), paramQuery.getNodeId(), setting.get("setting")));
            }
            update(list, ParamType.DB);
        } finally {
            clusterManager.pool();
        }
    }

    private void updateOsParamInfo(ParamQuery paramQuery) {
        try {
            var node = clusterManager.getOpsNodeById(paramQuery.getNodeId());
            if (node == null) {
                throw new InstanceException(MessageSourceUtils.get("node.tip"));
            }
            var ssh = SshSessionUtils.connect(node.getPublicIp(), node.getHostPort(), "root",
                    encryptionUtils.decrypt(paramQuery.getPassword()));
            String[] values = ssh.execute("sysctl -a").split(String.valueOf(StrUtil.C_LF));
            var list = new ArrayList<ParamValueInfoDO>();
            for (int n = 0; n < values.length; n++) {
                String name = values[n].substring(0, values[n].lastIndexOf(CommonConstants.EQUAL_SYMBOL)).trim();
                String paramData = values[n].substring(values[n].indexOf(CommonConstants.EQUAL_SYMBOL) + 1).trim();
                var paramInfo = getParamInfo(ParamType.OS, name);
                if (paramInfo == null) {
                    continue;
                }
                list.add(new ParamValueInfoDO(paramInfo.getId(), paramQuery.getNodeId(), paramData));
            }
            update(list, ParamType.OS);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InstanceException(MessageSourceUtils.get("password.tip"), e);
        }
    }

    private void update(ArrayList<ParamValueInfoDO> list, ParamType t) {
        var ids = getIds(t);
        paramValueInfoService.remove(Wrappers.lambdaQuery(ParamValueInfoDO.class)
                .in(ParamValueInfoDO::getSid, ids));
        paramValueInfoService.saveBatch(list);
    }

    @Override
    public void afterPropertiesSet() {
        var list = list();
        LIST.clear();
        LIST.addAll(list);
        IDS.clear();
        for (ParamInfoDO paramInfoDO : list) {
            IDS.put(paramInfoDO.getId(), paramInfoDO);
        }
        for (ParamType v : ParamInfoDO.ParamType.values()) {
            MAP.put(v, new HashMap<>());
        }
        for (ParamInfoDO paramInfoDO : list) {
            MAP.get(paramInfoDO.getParamType()).put(paramInfoDO.getParamName(), paramInfoDO);
        }
    }

    /**
     * getParamInfo
     *
     * @param type ParamType
     * @param name String
     * @return ParamInfo
     */
    public ParamInfoDO getParamInfo(ParamInfoDO.ParamType type, String name) {
        return MAP.get(type).get(name);
    }

    /**
     * getById
     *
     * @param id Integer
     * @return ParamInfo
     */
    public ParamInfoDO getById(Integer id) {
        return IDS.get(id);
    }

    /**
     * getAll
     *
     * @return List<ParamInfo>
     */
    public List<ParamInfoDO> getAll() {
        return LIST;
    }

    /**
     * getIds
     *
     * @param t ParamType
     * @return List<Integer>
     */
    public List<Integer> getIds(ParamType t) {
        return MAP.get(t).values().stream().map(ParamInfoDO::getId).collect(Collectors.toList());
    }
}