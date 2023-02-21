package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;

import java.util.Date;
import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskStatusRecordService extends IService<MigrationTaskStatusRecord> {
    void saveRecord(Integer taskId, Integer statusId, String title , Date time);

    List<MigrationTaskStatusRecord> selectByTaskId(Integer taskId);
}
