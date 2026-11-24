/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.model.dto;

import com.nctigba.observability.sql.model.vo.point.OptionVO;
import com.nctigba.observability.sql.model.vo.point.ThresholdVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DiagnosisTaskDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class DiagnosisTaskDTO {
    private String clusterId;
    private String nodeId;
    private String taskName;
    private String dbName;
    private String schemaName;
    private String sqlId;
    private String sql;
    private Date hisDataStartTime;
    private Date hisDataEndTime;
    List<OptionVO> configs;
    List<ThresholdVO> thresholds;
    private String diagnosisType;
}
