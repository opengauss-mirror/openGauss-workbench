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
 * IModelingDataFlowService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/IModelingDataFlowService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;

import java.util.List;
import java.util.Map;

/**
* @author LZW
* @description modeling_data_flow
* @createDate 2022-08-10 11:41:13
*/
public interface IModelingDataFlowService extends IService<ModelingDataFlowEntity> {

    IPage<ModelingDataFlowEntity> selectList(IPage<ModelingDataFlowEntity> page, String name);

    Long insertDataFlow(ModelingDataFlowEntity dataFlowData);

    int deleteDataFlowByIds(String[] flowIds);

    int updateDataFlow(ModelingDataFlowEntity dataFlowData);

    List<ModelingDataFlowEntity> findByName(String name);

    List<ModelingDataFlowEntity> queryProcessInfo();

    List<Map<String, Integer>> queryGroupByType();
}
