package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;
import org.opengauss.admin.plugin.mapper.MigrationTaskStatusRecordMapper;
import org.opengauss.admin.plugin.service.MigrationTaskOperateRecordService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    public void saveRecord(Integer taskId, Integer statusId, String title, Date time) {
        LambdaQueryWrapper<MigrationTaskStatusRecord> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskStatusRecord::getTaskId, taskId);
        query.last("limit 1").orderByDesc(MigrationTaskStatusRecord::getCreateTime);
        MigrationTaskStatusRecord lastRecord = this.getOne(query);
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
    public List<MigrationTaskStatusRecord> selectByTaskId(Integer taskId) {
        return migrationTaskStatusRecordMapper.selectByTaskId(taskId);
    }

}
