package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.enums.TaskOperate;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskOperateRecordService extends IService<MigrationTaskOperateRecord> {

    void saveRecord(Integer taskId,TaskOperate operate, String operUser);

    MigrationTaskOperateRecord getLastRecordByTaskId(Integer taskId);

    MigrationTaskOperateRecord getRecordByTaskIdAndOperType(Integer taskId, Integer oerType);

    void deleteByTaskIds(List<Integer> taskIds);
}
