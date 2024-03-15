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
 *  CollectTemplateMetricsMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/mapper/CollectTemplateMetricsMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.nctigba.observability.instance.model.entity.CollectTemplateMetricsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.system.domain.SysRoleMenu;

import java.util.List;

/**
 * <p>
 * Mapper
 * </p>
 *
 * @since 2023-11-02
 */
@DS("embedded")
@Mapper
public interface CollectTemplateMetricsMapper extends BaseMapper<CollectTemplateMetricsDO> {
    /**
     * Add collect template matrics in batches
     *
     * @param collectTemplateMetricsDOS CollectTemplateMetricsList
     */
    public int insertBatchTemplateMetrics(List<CollectTemplateMetricsDO> collectTemplateMetricsDOS);
}
