package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.system.domain.SysTask;

import java.util.List;
import java.util.Map;

/**
 * Task Service Interface
 *
 * @author xielibo
 */
public interface ISysTaskService extends IService<SysTask> {

    IPage<SysTask> selectList(IPage<SysTask> page, SysTaskDto task);

    List<SysTask> selectListAll(SysTaskDto task);

    Map<String, Object> getDetailById(Integer taskId);

    void deleteTask(Integer[] ids);

    void updateStatus(Integer id, SysTaskStatus taskStatus);

    void startTask(Integer id);

    void stopTask(Integer id);

    void refreshTaskStatus();
}
