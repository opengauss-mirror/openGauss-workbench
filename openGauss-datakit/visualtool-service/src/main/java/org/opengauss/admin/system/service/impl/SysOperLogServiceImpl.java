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
 * SysOperLogServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysOperLogServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.system.domain.SysOperLog;
import org.opengauss.admin.system.mapper.SysOperLogMapper;
import org.opengauss.admin.system.service.ISysOperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * OperLog Service
 *
 * @author xielibo
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    @Autowired
    private SysOperLogMapper operLogMapper;

    /**
     * Save Operlog
     *
     */
    @Override
    public void insertOperlog(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    /**
     * Query the collection of system operation logs
     *
     * @param operLog operLog
     */
    @Override
    public IPage<SysOperLog> selectOperLogList(IPage<SysOperLog> page, SysOperLog operLog) {
        LambdaQueryWrapper<SysOperLog> query = new QueryWrapper<SysOperLog>().lambda();
        query.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle());
        query.eq(operLog.getBusinessType() != null, SysOperLog::getBusinessType, operLog.getBusinessType());
        query.in(operLog.getBusinessTypes() != null, SysOperLog::getBusinessType, operLog.getBusinessTypes());
        query.eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus());
        query.like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName());
        query.orderByDesc(SysOperLog::getOperId);
        return operLogMapper.selectPage(page, query);
    }

    /**
     * Delete System Operation Logs in Batches
     *
     * @param operIds operIds
     */
    @Override
    public int deleteOperLogByIds(String[] operIds) {
        return operLogMapper.deleteBatchIds(Arrays.asList(operIds));
    }

    /**
     * Query operation log details
     *
     * @param operId operId
     */
    @Override
    public SysOperLog selectOperLogById(String operId) {
        return operLogMapper.selectById(operId);
    }

    /**
     * clear operation log
     */
    @Override
    public void cleanOperLog() {
        operLogMapper.delete(new QueryWrapper<SysOperLog>().lambda().eq(SysOperLog::getStatus, UserConstants.NORMAL).or().eq(SysOperLog::getStatus, UserConstants.EXCEPTION));
    }
}
