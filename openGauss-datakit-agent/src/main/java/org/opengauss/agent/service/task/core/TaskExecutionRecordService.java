/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.service.task.core;

import cn.hutool.core.io.FileUtil;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

import org.opengauss.agent.entity.TaskExecution;

import java.io.File;
import java.util.List;

/**
 * TaskExecutionException
 *
 * @author: wangchao
 * @Date: 2025/4/9 17:26
 * @Description: TaskExecutionRecordService
 * @since 7.0.0-RC2
 **/
@Unremovable
@ApplicationScoped
public class TaskExecutionRecordService {
    private static final File HISTORY = new File("history.txt");

    /**
     * save record to history.txt file
     *
     * @param record record
     */
    public void save(String record) {
        FileUtil.appendUtf8Lines(List.of(record), HISTORY);
    }

    /**
     * save record to history.txt file
     *
     * @param record record
     */
    public void save(TaskExecution record) {
        FileUtil.appendUtf8Lines(List.of(record), HISTORY);
    }
}
