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
 * IModelingVisualizationSnapshotsService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/IModelingVisualizationSnapshotsService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationSnapshotsEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_snapshots
* @createDate 2022-09-23 13:44:43
*/
public interface IModelingVisualizationSnapshotsService extends IService<ModelingVisualizationSnapshotsEntity> {
    IPage<ModelingVisualizationSnapshotsEntity> selectList(IPage<ModelingVisualizationSnapshotsEntity> page, ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);

    public List<ModelingVisualizationSnapshotsEntity> getByDataFlowId(Long dataFlowId);
    int insertDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);
    int deleteByIds(String[] flowIds);
    int updateDataFlow(ModelingVisualizationSnapshotsEntity modelingVisualizationSnapshotsEntity);
}
