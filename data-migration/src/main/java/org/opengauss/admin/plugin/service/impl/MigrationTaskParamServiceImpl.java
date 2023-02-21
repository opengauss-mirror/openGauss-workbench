package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskParam;
import org.opengauss.admin.plugin.mapper.MigrationTaskParamMapper;
import org.opengauss.admin.plugin.service.MigrationTaskParamService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskParamServiceImpl extends ServiceImpl<MigrationTaskParamMapper, MigrationTaskParam> implements MigrationTaskParamService {


    @Override
    public List<MigrationTaskParam> selectByTaskId(Integer taskId) {
        LambdaQueryWrapper<MigrationTaskParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskParam::getTaskId, taskId);
        return this.list(query);
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskParam::getMainTaskId, mainTaskId);
        this.remove(query);
    }

}
