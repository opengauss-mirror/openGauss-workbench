/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationTaskAlert;
import org.opengauss.admin.plugin.dto.MigrationTaskAlertDto;

import java.util.List;

/**
 * migration task alert mapper
 *
 * @since 2024/12/16
 */
@Mapper
public interface MigrationTaskAlertMapper extends BaseMapper<MigrationTaskAlert> {
    /**
     * count group alert
     *
     * @param taskId task id
     * @return group alert count
     */
    long countGroupAlertByTaskId(@Param("taskId") int taskId);

    /**
     * count group alert in phase
     *
     * @param taskId task id
     * @param phaseId phase id
     * @return group alert in phase
     */
    long countGroupAlertInPhase(@Param("taskId") int taskId, @Param("phaseId") int phaseId);

    /**
     * get group alert dto in phase of task
     *
     * @param taskId task id
     * @param phaseId phase id
     * @return migration task alert dto list
     */
    List<MigrationTaskAlertDto> getGroupAlertDto(@Param("taskId") int taskId, @Param("phaseId") int phaseId);

    /**
     * get group alert id list by the same group alert
     *
     * @param alert alert entity
     * @return group alert id list
     */
    List<Integer> getGroupAlertIds(MigrationTaskAlert alert);
}
