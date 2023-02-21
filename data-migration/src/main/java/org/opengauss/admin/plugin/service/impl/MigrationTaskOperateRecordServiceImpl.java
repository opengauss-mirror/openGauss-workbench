package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.mapper.MigrationTaskOperateRecordMapper;
import org.opengauss.admin.plugin.service.MigrationTaskOperateRecordService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskOperateRecordServiceImpl extends ServiceImpl<MigrationTaskOperateRecordMapper, MigrationTaskOperateRecord> implements MigrationTaskOperateRecordService {



    @Override
    public void saveRecord(Integer taskId, String title, String operUser) {
        MigrationTaskOperateRecord record = new MigrationTaskOperateRecord();
        record.setTitle(title);
        record.setTaskId(taskId);
        record.setOperUser(operUser);
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

}
