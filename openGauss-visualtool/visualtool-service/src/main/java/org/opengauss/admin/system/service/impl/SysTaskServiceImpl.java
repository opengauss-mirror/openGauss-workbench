package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.spring.extract.ExtractFactory;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.system.domain.SysTask;
import org.opengauss.admin.system.mapper.SysTaskMapper;
import org.opengauss.admin.system.plugin.extract.TaskExtract;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Task Service
 *
 * @author xielibo
 */
@Service
@Slf4j
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements ISysTaskService {

    @Autowired
    private SysTaskMapper sysTaskMapper;

    @Autowired
    private ExtractFactory extractFactory;

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

    @Override
    public void deleteTask(Integer[] ids){
        Arrays.asList(ids).stream().forEach(i -> {
            SysTask task = this.getById(i);
            List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
            if (extractByInterClass.size() > 0) {
                log.info("Get the TaskExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
                extractByInterClass.get(0).deleteTask(i);
                this.removeById(i);
            } else {
                log.error("No implementation found");
            }
        });
    }

    @Override
    public void startTask(Integer id){
        SysTask task = this.getById(id);
        List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the TaskExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).startTask(id);
        } else {
            log.error("No implementation found");
        }
        task.setExecStatus(SysTaskStatus.PROCESSING.getCode());
        this.updateById(task);
    }

    @Override
    public void stopTask(Integer id){
        SysTask task = this.getById(id);
        List<TaskExtract> extractByInterClass = extractFactory.getExtractByInterClass(task.getPluginId(), TaskExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the TaskExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).stopTask(id);
        } else {
            log.error("No implementation found");
        }
        task.setExecStatus(SysTaskStatus.STOP.getCode());
        this.updateById(task);
    }
}
