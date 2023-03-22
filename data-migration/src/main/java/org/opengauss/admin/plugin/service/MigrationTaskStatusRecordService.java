package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskStatusRecordService extends IService<MigrationTaskStatusRecord> {
    void saveTaskRecord(Integer taskId, List<Map<String, Object>> statusRecord);

    void saveRecord(Integer taskId, Integer statusId, String title , Date time);

    MigrationTaskStatusRecord getLastByTaskId(Integer taskId);

    List<MigrationTaskStatusRecord> selectByTaskId(Integer taskId);

    void deleteByTaskIds(List<Integer> taskIds);
}
