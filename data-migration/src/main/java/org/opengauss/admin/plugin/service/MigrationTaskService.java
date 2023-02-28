package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.enums.TaskStatus;

import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskService extends IService<MigrationTask> {

    IPage<MigrationTask> selectList(IPage<MigrationTask> page, Integer mainTaskId);

    void deleteByMainTaskId(Integer mainTaskId);

    List<MigrationTask> listByMainTaskId(Integer mainTaskId);

    Map<String, Object> getTaskDetailById(Integer taskId);

    Integer countRunningByTargetDb(String targetDb);

    Integer countRunningByHostId(String hostId);

    List<MigrationTask> listRunningTaskByHostId(String hostId);

    List<MigrationTask> listTaskByStatus(TaskStatus taskStatus);

    Integer countTaskByStatus(TaskStatus taskStatus);

    void updateStatus(Integer id, TaskStatus taskStatus);

    List<Map<String, Object>> countByMainTaskIdGroupByModel(Integer mainTaskId);

    void runTask(MigrationTaskHostRef h, MigrationTask t, List<MigrationTaskGlobalParam> globalParams);

    void doOfflineTaskRunScheduler();
}
