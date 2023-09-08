/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.query.TaskQuery;
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
    Integer add(HisDiagnosisTaskDTO hisDiagnosisTaskDTO);

    void start(int taskId);

    List<OptionQuery> getOption(String diagnosisType);

    /**
     * Query all tasks by page
     *
     * @param query task query info
     * @return IPage
     */
    IPage<HisDiagnosisTask> selectByPage(TaskQuery query);

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
    void explainBeforeAfter(HisDiagnosisTask task, ArrayList<String> rsList);

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
    HisDiagnosisTask selectById(int taskId);

    /**
     * Query explain
     *
     * @param nodeId node id
     * @param sqlId sql id
     * @return IPage
     */
    JSONObject plan(String nodeId, String sqlId);
}
