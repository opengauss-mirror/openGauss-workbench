package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskExecResultDetail;
import org.opengauss.admin.plugin.mapper.MigrationTaskExecResultDetailMapper;
import org.opengauss.admin.plugin.service.MigrationTaskExecResultDetailService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskExecResultDetailServiceImpl extends ServiceImpl<MigrationTaskExecResultDetailMapper, MigrationTaskExecResultDetail> implements MigrationTaskExecResultDetailService {


    @Override
    public void saveOrUpdateByTaskId(Integer taskId, String process, Integer processType) {
        LambdaQueryWrapper<MigrationTaskExecResultDetail> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskExecResultDetail::getTaskId, taskId).eq(MigrationTaskExecResultDetail::getProcessType, processType);
        query.last("limit 1");
        MigrationTaskExecResultDetail resultDetail = this.getOne(query);
        if (resultDetail == null) {
            resultDetail = MigrationTaskExecResultDetail.builder()
                    .taskId(taskId).processType(processType).execResultDetail(process).createTime(new Date()).build();
            this.save(resultDetail);
        } else {
            resultDetail.setExecResultDetail(process);
            resultDetail.setCreateTime(new Date());
            this.updateById(resultDetail);
        }
    }

    @Override
    public MigrationTaskExecResultDetail getByTaskIdAndProcessType(Integer taskId, Integer processType) {
        LambdaQueryWrapper<MigrationTaskExecResultDetail> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskExecResultDetail::getTaskId, taskId).eq(MigrationTaskExecResultDetail::getProcessType, processType);
        query.last("limit 1");
        return this.getOne(query);
    }

    @Override
    public void deleteByTaskIds(List<Integer> taskIds) {
        LambdaQueryWrapper<MigrationTaskExecResultDetail> query = new LambdaQueryWrapper<>();
        query.in(MigrationTaskExecResultDetail::getTaskId, taskIds);
        this.remove(query);
    }

}
