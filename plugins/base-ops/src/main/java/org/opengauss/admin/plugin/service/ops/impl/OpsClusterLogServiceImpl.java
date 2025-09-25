/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterLogServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/OpsClusterLogServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterOperateLog;
import org.opengauss.admin.plugin.enums.ops.ClusterOperateTypeEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterOperateLogMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * OpsClusterLogServiceImpl
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Service("opsClusterLogService")
public class OpsClusterLogServiceImpl implements IOpsClusterLogService {

    @Resource
    private OpsClusterOperateLogMapper opsClusterOperateLogMapper;

    @Override
    public void insertOperateLog(OpsClusterOperateLog operateLog) {
        opsClusterOperateLogMapper.insert(operateLog);
    }

    @Override
    public IPage<OpsClusterOperateLog> selectOperateLogList(IPage<OpsClusterOperateLog> page, OpsClusterOperateLog operateLog) {
        LambdaQueryWrapper<OpsClusterOperateLog> query = new QueryWrapper<OpsClusterOperateLog>().lambda();
        query.select(OpsClusterOperateLog::getOperateId,
                OpsClusterOperateLog::getClusterId,
                OpsClusterOperateLog::getOperateType,
                OpsClusterOperateLog::getCreateTime);
        query.eq(StrUtil.isNotBlank(operateLog.getClusterId()), OpsClusterOperateLog::getClusterId, operateLog.getClusterId());
        query.eq(Objects.nonNull(operateLog.getOperateType()), OpsClusterOperateLog::getOperateType, operateLog.getOperateType());
        query.orderByDesc(OpsClusterOperateLog::getClusterId);
        query.orderByDesc(OpsClusterOperateLog::getCreateTime);
        return opsClusterOperateLogMapper.selectPage(page, query);
    }

    @Override
    public int deleteOperateLogByIds(String[] operateIds) {
        return opsClusterOperateLogMapper.deleteBatchIds(List.of(operateIds));
    }

    @Override
    public int deleteOperateLogByClusterId(String clusterId) {
        LambdaQueryWrapper<OpsClusterOperateLog> deleteWrapper = Wrappers.lambdaQuery(OpsClusterOperateLog.class);
        deleteWrapper.eq(OpsClusterOperateLog::getClusterId, clusterId);
        return opsClusterOperateLogMapper.delete(deleteWrapper);
    }

    @Override
    public OpsClusterOperateLog selectOperateLogById(String operateId) {
        return opsClusterOperateLogMapper.selectById(operateId);
    }

    private static final String OPERATE_LOG_SEPARATOR = " -- ";
    private static final String OPERATE_LOG_LINE_SEPARATOR = "\\\\r\\\\n";

    @Override
    public String queryClusterOperateLog(String clusterId) {
        LambdaQueryWrapper<OpsClusterOperateLog> wrapper = Wrappers.lambdaQuery(OpsClusterOperateLog.class);
        wrapper.eq(OpsClusterOperateLog::getClusterId, clusterId);
        List<OpsClusterOperateLog> logList = opsClusterOperateLogMapper.selectList(wrapper);
        StringBuffer logBuffer = new StringBuffer();
        logList.forEach(operateLog -> {
            logBuffer.append(operateLog.getOperateTime()).append(OPERATE_LOG_SEPARATOR)
                    .append(operateLog.getOperateType()).append(OPERATE_LOG_SEPARATOR)
                    .append(convertOperateLog(operateLog.getOperateType(), operateLog.getOperateLog()))
                    .append(System.lineSeparator());
        });
        return logBuffer.toString();
    }

    private String convertOperateLog(ClusterOperateTypeEnum operateType, String operateLog) {
        if (Objects.equals(operateType, ClusterOperateTypeEnum.CHECK_ENVIRONMENT)) {
            return formatJsonOperateLog(operateLog);
        } else if (Objects.equals(operateType, ClusterOperateTypeEnum.INSTALL)) {
            return operateLog.replaceAll(OPERATE_LOG_LINE_SEPARATOR, System.lineSeparator());
        } else {
            return operateLog;
        }
    }

    private static String formatJsonOperateLog(String operateLog) {
        try {
            return JSONObject.toJSONString(JSONObject.parseObject(operateLog), true);
        } catch (Exception e) {
            return operateLog;
        }
    }
}
