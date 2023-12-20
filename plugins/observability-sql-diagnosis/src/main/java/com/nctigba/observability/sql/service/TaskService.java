/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TaskService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/TaskService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface TaskService {
    /**
     * Add diagnosis task
     *
     * @param diagnosisTaskDTO Diagnosis task
     * @return Integer
     */
    Integer add(DiagnosisTaskDTO diagnosisTaskDTO);

    /**
     * Start run diagnosis task
     *
     * @param taskId Diagnosis task id
     */
    void start(int taskId);

    /**
     * Gets the options for diagnostic tasks
     *
     * @param diagnosisType Diagnosis type
     * @return List
     */
    List<OptionVO> getOption(String diagnosisType);

    /**
     * Query all tasks by page
     *
     * @param query task query info
     * @return IPage
     */
    IPage<DiagnosisTaskDO> selectByPage(TaskQuery query);

    /**
     * Delete task by id
     *
     * @param taskId task id
     */
    void delete(int taskId);

    /**
     * Analysis explain
     *
     * @param task task info
     * @param rsList explain info
     */
    void explainAfter(DiagnosisTaskDO task, ArrayList<String> rsList);

    /**
     * Analysis ebpf
     *
     * @param taskId task id
     * @param pointName point name
     * @param file ebpf result
     */
    void bccResult(String taskId, String pointName, MultipartFile file);

    /**
     * Query task by id
     *
     * @param taskId task id
     * @return task info
     */
    DiagnosisTaskDO selectById(int taskId);
}