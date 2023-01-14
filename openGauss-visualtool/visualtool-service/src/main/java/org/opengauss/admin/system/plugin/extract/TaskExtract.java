package org.opengauss.admin.system.plugin.extract;

import org.opengauss.admin.system.plugin.beans.BasePluginTaskDetailDto;
import org.opengauss.admin.system.plugin.beans.TaskExecProgressDto;

import java.util.List;

/**
 * @className: TaskExtract
 * @description: Used to receive messages from clients
 * @author: xielibo
 * @date: 2022-11-14 21:33
 **/
public interface TaskExtract {

    /**
     * 获取子任务列表
     * @param taskId
     * @return
     */
    public List<BasePluginTaskDetailDto> getPluginTask(Integer taskId);

    /**
     * 获取任务状态，从插件中的子任务汇总，具体逻辑由插件实现，返回平台定义的插件状态枚举
     * @param taskId
     * @return
     */
    public TaskExecProgressDto getTaskStatus(Integer taskId);


}
