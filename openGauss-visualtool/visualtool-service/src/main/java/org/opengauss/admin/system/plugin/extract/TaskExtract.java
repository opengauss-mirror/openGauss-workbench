package org.opengauss.admin.system.plugin.extract;

import org.opengauss.admin.system.plugin.beans.BasePluginTaskDetailDto;
import org.opengauss.admin.system.plugin.beans.MigrationTaskDetail;
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
     * Get subtask details
     * @param taskId
     * @return
     */
    public List<MigrationTaskDetail> getPluginTask(Integer taskId);

    /**
     * Get the task status, summarize from the subtasks in the plug-in,
     * the specific logic is implemented by the plug-in, and return the plug-in status enumeration defined by the platform
     * @param taskId
     * @return
     */
    public TaskExecProgressDto getTaskStatus(Integer taskId);


    /**
     * start task
     * @param taskId
     * @return
     */
    public void startTask(Integer taskId);


    /**
     * stop task
     * @param taskId
     * @return
     */
    public void stopTask(Integer taskId);

    /**
     * delete task
     * @param taskId
     * @return
     */
    public void deleteTask(Integer taskId);
}
