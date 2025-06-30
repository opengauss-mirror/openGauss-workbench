/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * MigrationTaskStatusRecordService.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationTaskStatusRecordService.java
 *
 * -------------------------------------------------------------------------
 */


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

    /**
     * Get the latest record of the task
     *
     * @param taskId taskId
     * @return record
     */
    MigrationTaskStatusRecord getLagerStatusByTaskId(Integer taskId);

    List<MigrationTaskStatusRecord> selectByTaskId(Integer taskId);

    void deleteByTaskIds(List<Integer> taskIds);
}
