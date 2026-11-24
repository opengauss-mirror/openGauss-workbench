/*
 * Copyright (c) 2022 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.modeling.visualization;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationReportsEntity;

import java.util.List;

/**
* @author LZW
* @description modeling_visualization_reports
* @createDate 2022-09-29 13:12:50
*/
public interface IModelingVisualizationReportsService extends IService<ModelingVisualizationReportsEntity> {

    public List<ModelingVisualizationReportsEntity> getByDataFlowIdAndName(Long dataFlowId,String name);
    public int add(ModelingVisualizationReportsEntity modelingVisualizationParams);
    public int deleteByIds(String[] reportIds);
    public ModelingVisualizationReportsEntity selectById(Integer reportId);
    public int update(ModelingVisualizationReportsEntity modelingVisualizationParams);

}
