/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.entity.ParamInfo.ParamType;
import com.nctigba.observability.instance.entity.ParamValueInfo;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.mapper.ParamInfoMapper;
import com.nctigba.observability.instance.mapper.ParamValueInfoMapper;
import com.nctigba.observability.instance.model.ParamQuery;
import com.nctigba.observability.instance.util.MessageSourceUtil;
import com.nctigba.observability.instance.util.SshSession;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParamInfoService {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private ParamValueInfoMapper paramValueInfoMapper;
    @Autowired
    private DbConfigMapper dbConfigMapper;

    public List<ParamInfoDTO> getParamInfo(ParamQuery paramQuery) {
        if ("1".equals(paramQuery.getIsRefresh())) {
            if (StrUtil.isBlank(paramQuery.getPassword())) {
                return null;
            }
            this.updateOsParamInfo(paramQuery);
            this.updateDatabaseParamInfo(paramQuery);
            paramValueInfoMapper.refresh(paramQuery.getNodeId());
        }
        return paramValueInfoMapper.query(paramQuery.getNodeId());
    }

    private void updateDatabaseParamInfo(ParamQuery paramQuery) {
        try {
            clusterManager.setCurrentDatasource(paramQuery.getNodeId(), null);
            var list = new ArrayList<ParamValueInfo>();
            for (Map<String, String> setting : dbConfigMapper.settings()) {
                var paramInfo = ParamInfoMapper.getParamInfo(ParamType.DB, setting.get("name"));
                if (paramInfo == null)
                    continue;
                list.add(new ParamValueInfo(paramInfo.getId(), paramQuery.getNodeId(), setting.get("setting")));
            }
            update(list, ParamType.DB);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new InstanceException(MessageSourceUtil.get("connect.database.tip"), e);
        } finally {
            clusterManager.pool();
        }

    }

    private void updateOsParamInfo(ParamQuery paramQuery) {
        try {
            var node = clusterManager.getOpsNodeById(paramQuery.getNodeId());
            if (node == null)
                throw new InstanceException(MessageSourceUtil.get("node.tip"));
            var ssh = SshSession.connect(node.getPublicIp(), node.getHostPort(), "root",
                    encryptionUtils.decrypt(paramQuery.getPassword()));
            String[] values = ssh.execute("sysctl -a").split("\n");
            var list = new ArrayList<ParamValueInfo>();
            for (int n = 0; n < values.length; n++) {
                String name = values[n].substring(0, values[n].lastIndexOf(CommonConstants.EQUAL_SYMBOL)).trim();
                String paramData = values[n].substring(values[n].indexOf(CommonConstants.EQUAL_SYMBOL) + 1).trim();
                var paramInfo = ParamInfoMapper.getParamInfo(ParamType.OS, name);
                if (paramInfo == null)
                    continue;
                list.add(new ParamValueInfo(paramInfo.getId(), paramQuery.getNodeId(), paramData));
            }
            update(list, ParamType.OS);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new InstanceException(MessageSourceUtil.get("password.tip"), e);
        }
    }

    private void update(ArrayList<ParamValueInfo> list, ParamType t) {
        var ids = ParamInfoMapper.getIds(t);
        ParamValueInfoMapper.delBySids(ids);
        ParamValueInfoMapper.insertBatch(list);
    }
}