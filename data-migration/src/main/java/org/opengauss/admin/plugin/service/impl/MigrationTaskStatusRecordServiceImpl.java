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
 * MigrationTaskStatusRecordServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskStatusRecordServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.constants.TaskConstant;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;
import org.opengauss.admin.plugin.mapper.MigrationTaskStatusRecordMapper;
import org.opengauss.admin.plugin.service.MigrationTaskOperateRecordService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskStatusRecordServiceImpl extends ServiceImpl<MigrationTaskStatusRecordMapper, MigrationTaskStatusRecord> implements MigrationTaskStatusRecordService {


    @Autowired
    private MigrationTaskOperateRecordService migrationTaskOperateRecordService;

    @Autowired
    private MigrationTaskStatusRecordMapper migrationTaskStatusRecordMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskRecord(Integer taskId, List<Map<String, Object>> statusRecord) {
        LambdaQueryWrapper<MigrationTaskStatusRecord> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskStatusRecord::getTaskId, taskId);
        this.remove(query);
        Map<Integer, List<Map<String, Object>>> status = statusRecord.stream().collect(Collectors.groupingBy(r -> MapUtil.getInt(r, "status")));
        status.entrySet().stream().forEach(m -> {
            Integer operateType = TaskConstant.TASK_STATUS_OPERATE_MAPPING.get(m.getKey());
            if (operateType != null) {
                MigrationTaskStatusRecord record = new MigrationTaskStatusRecord();
                MigrationTaskOperateRecord lastOperateRecord = migrationTaskOperateRecordService.getRecordByTaskIdAndOperType(taskId, operateType);
                if (lastOperateRecord != null) {
                    record.setOperateId(lastOperateRecord.getId());
                }
                record.setStatusId(m.getKey());
                record.setTaskId(taskId);
                if (m.getValue().size() > 0) {
                    Long timestamp = MapUtil.getLong(m.getValue().get(0), "timestamp");
                    record.setCreateTime(DateUtil.date(timestamp));
                }
                this.save(record);
            }
        });

    }

    @Override
    public void saveRecord(Integer taskId, Integer statusId, String title, Date time) {
        MigrationTaskStatusRecord lastRecord = getLastByTaskId(taskId);
        if (lastRecord == null || !lastRecord.getStatusId().equals(statusId)) {
            MigrationTaskStatusRecord record = new MigrationTaskStatusRecord();
            record.setTitle(title);
            record.setTaskId(taskId);
            record.setStatusId(statusId);
            record.setCreateTime(time);
            MigrationTaskOperateRecord lastOperateRecord = migrationTaskOperateRecordService.getLastRecordByTaskId(taskId);
            if (lastOperateRecord != null) {
                record.setOperateId(lastOperateRecord.getId());
            }
            this.save(record);
        }
    }

    @Override
    public MigrationTaskStatusRecord getLastByTaskId(Integer taskId) {
        LambdaQueryWrapper<MigrationTaskStatusRecord> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskStatusRecord::getTaskId, taskId);
        query.last("limit 1").orderByDesc(MigrationTaskStatusRecord::getCreateTime);
        MigrationTaskStatusRecord lastRecord = this.getOne(query);
        return lastRecord;
    }

    @Override
    public List<MigrationTaskStatusRecord> selectByTaskId(Integer taskId) {
        return migrationTaskStatusRecordMapper.selectByTaskId(taskId);
    }

    @Override
    public void deleteByTaskIds(List<Integer> taskIds) {
        LambdaQueryWrapper<MigrationTaskStatusRecord> query = new LambdaQueryWrapper<>();
        query.in(MigrationTaskStatusRecord::getTaskId, taskIds);
        this.remove(query);
    }

}
