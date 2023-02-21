package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;
import org.opengauss.admin.plugin.dto.MigrationTaskDto;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Task Service Interface
 *
 * @author xielibo
 */
public interface MigrationMainTaskService extends IService<MigrationMainTask> {

    IPage<MigrationMainTask> selectList(IPage<MigrationMainTask> page, MigrationMainTaskDto task);

    List<String> selectCreateUsers();

    Map<String, Object> getDetailById(Integer taskId);

    void saveTask(MigrationTaskDto taskDto);

    @Transactional
    AjaxResult updateTask(MigrationTaskDto taskDto);

    void deleteTask(Integer[] ids);

    void updateStatus(Integer id, MainTaskStatus taskStatus);

    AjaxResult startTask(Integer id);

    void finishTask(Integer id);

    AjaxResult finishSubTask(Integer id);

    AjaxResult stopSubTaskIncremental(Integer id);

    AjaxResult startSubTaskReverse(Integer id);

    void refreshTaskStatusByPortal(Integer taskId);

}
