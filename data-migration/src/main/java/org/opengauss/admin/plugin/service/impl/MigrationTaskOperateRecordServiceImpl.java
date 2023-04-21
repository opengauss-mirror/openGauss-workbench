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
 * MigrationTaskOperateRecordServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskOperateRecordServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.mapper.MigrationTaskOperateRecordMapper;
import org.opengauss.admin.plugin.service.MigrationTaskOperateRecordService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskOperateRecordServiceImpl extends ServiceImpl<MigrationTaskOperateRecordMapper, MigrationTaskOperateRecord> implements MigrationTaskOperateRecordService {



    @Override
    public void saveRecord(Integer taskId, TaskOperate operate, String operUser) {
        MigrationTaskOperateRecord record = new MigrationTaskOperateRecord();
        record.setTitle(operate.getCommand());
        record.setTaskId(taskId);
        record.setOperUser(operUser);
        record.setOperType(operate.getCode());
        record.setOperTime(new Date());
        this.save(record);
    }

    @Override
    public MigrationTaskOperateRecord getLastRecordByTaskId(Integer taskId) {
        LambdaQueryWrapper<MigrationTaskOperateRecord> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskOperateRecord::getTaskId, taskId);
        query.last(" limit 1").orderByDesc(MigrationTaskOperateRecord::getOperTime);
        return getOne(query);
    }

    @Override
    public MigrationTaskOperateRecord getRecordByTaskIdAndOperType(Integer taskId, Integer oerType) {
        LambdaQueryWrapper<MigrationTaskOperateRecord> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskOperateRecord::getTaskId, taskId).eq(MigrationTaskOperateRecord::getOperType, oerType);
        query.last(" limit 1");
        return getOne(query);
    }

    @Override
    public void deleteByTaskIds(List<Integer> taskIds) {
        LambdaQueryWrapper<MigrationTaskOperateRecord> query = new LambdaQueryWrapper<>();
        query.in(MigrationTaskOperateRecord::getTaskId, taskIds);
        this.remove(query);
    }

}
