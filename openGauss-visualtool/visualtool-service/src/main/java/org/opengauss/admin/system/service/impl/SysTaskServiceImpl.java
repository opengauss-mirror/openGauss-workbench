package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.mapper.SysTaskMapper;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Task Service
 *
 * @author xielibo
 */
@Service
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements ISysTaskService {

    @Autowired
    private SysTaskMapper sysTaskMapper;

    /**
     * Query the task list by page
     *
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public IPage<SysTask> selectList(IPage<SysTask> page, SysTaskDto task) {
        return sysTaskMapper.selectTaskPage(page, task);
    }

    /**
     * Query all task
     *
     * @param task SysTask
     * @return SysTask
     */
    @Override
    public List<SysTask> selectListAll(SysTaskDto task) {
        return sysTaskMapper.selectTaskList(task);
    }

}
