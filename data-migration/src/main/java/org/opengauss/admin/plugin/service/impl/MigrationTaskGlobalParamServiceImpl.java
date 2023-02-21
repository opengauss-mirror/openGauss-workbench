package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;
import org.opengauss.admin.plugin.mapper.MigrationTaskGlobalParamMapper;
import org.opengauss.admin.plugin.service.MigrationTaskGlobalParamService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskGlobalParamServiceImpl extends ServiceImpl<MigrationTaskGlobalParamMapper, MigrationTaskGlobalParam> implements MigrationTaskGlobalParamService {

    @Override
    public List<MigrationTaskGlobalParam> selectByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskGlobalParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskGlobalParam::getMainTaskId, mainTaskId);
        return this.list(query);
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTaskGlobalParam> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTaskGlobalParam::getMainTaskId, mainTaskId);
        this.remove(query);
    }
}
