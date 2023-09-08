/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.service.history.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * CallbackController
 *
 * @author luomeng
 * @since 2023/8/11
 */
@RestController
@RequestMapping("/sqlDiagnosis/api/open/v1/diagnosisTasks")
@RequiredArgsConstructor
public class CallbackController {
    private final TaskService taskService;

    /**
     * Bcc diagnosis result
     *
     * @param id task id
     * @param type bcc call
     * @param file result data
     */
    @PostMapping(value = "/{id}/result", consumes = "multipart/*")
    public void diagnosisResult(@PathVariable String id, String type, MultipartFile file) {
        taskService.bccResult(id, type, file);
    }
}