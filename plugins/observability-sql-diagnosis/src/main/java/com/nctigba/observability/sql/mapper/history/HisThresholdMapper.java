/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.mapper.history;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.history.HisDiagnosisInit;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import org.apache.ibatis.annotations.Mapper;

/**
 * HisThresholdMapper
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Mapper
@DS(HisDiagnosisInit.HISTORY_DIAGNOSIS_THRESHOLD)
public interface HisThresholdMapper extends BaseMapper<HisDiagnosisThreshold> {
}
