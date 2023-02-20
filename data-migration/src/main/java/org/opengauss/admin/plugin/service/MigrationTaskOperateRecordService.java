package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskOperateRecord;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskOperateRecordService extends IService<MigrationTaskOperateRecord> {

    void saveRecord(Integer taskId, String title, String operUser);

    MigrationTaskOperateRecord getLastRecordByTaskId(Integer taskId);
}
