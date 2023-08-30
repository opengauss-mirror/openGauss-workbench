/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.plugin.domain.model.modeling;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Data
public class ModelingDataFlowOperatorParams implements Serializable {

    private String uid;

    private String cellType;

    private Integer level;

    private Boolean disable;

    private JSONObject data;

    private String belongsToMainOperatorId;

    private ModelingDataFlowOperatorParams dataSource;

    public void initDataSource(List<ModelingDataFlowOperatorParams> operatorParamsList, List<ModelingDataFlowLineParams> lineParamsList) {
        ModelingDataFlowLineParams foreLine = null;
        for (ModelingDataFlowLineParams line: lineParamsList) {
            if (Objects.equals(line.getTarget(), this.getUid()))
            {
                line.setLevel(this.getLevel() + 1);
                foreLine = line;
            }
        }

        if (foreLine != null)
        {
            for (ModelingDataFlowOperatorParams operator:operatorParamsList) {
                if (Objects.equals(operator.getUid(), foreLine.getSource()) && Objects.equals(operator.getCellType(), "dataSource"))
                {
                    this.setDataSource(operator);
                }
            }
        }

    }

    public void sortFore(List<ModelingDataFlowOperatorParams> operatorParamsList,List<ModelingDataFlowLineParams> lineParamsList)
    {
        ModelingDataFlowLineParams foreLine = null;
        for (ModelingDataFlowLineParams line: lineParamsList) {
            if (Objects.equals(line.getTarget(), this.getUid()))
            {
                line.setLevel(this.getLevel() + 1);
                foreLine = line;
            }
        }

        if (foreLine != null)
        {
            for (ModelingDataFlowOperatorParams operator:operatorParamsList) {
                if (Objects.equals(operator.getUid(), foreLine.getSource()))
                {
                    operator.setLevel(foreLine.getLevel() + 1);
                    operator.sortFore(operatorParamsList,lineParamsList);
                }
            }
        }
    }

    public void sortChildren(List<ModelingDataFlowOperatorParams> operatorParamsList,List<ModelingDataFlowLineParams> lineParamsList)
    {
        ModelingDataFlowLineParams nextLine = null;
        for (ModelingDataFlowLineParams line: lineParamsList) {
            if (Objects.equals(line.getSource(), this.getUid()))
            {
                line.setLevel(this.getLevel() + 1);
                nextLine = line;
            }
        }

        if (nextLine != null)
        {
            for (ModelingDataFlowOperatorParams operator:operatorParamsList) {
                if (Objects.equals(operator.getUid(), nextLine.getTarget()))
                {
                    operator.setBelongsToMainOperatorId(this.getBelongsToMainOperatorId());
                    operator.setLevel(nextLine.getLevel() + 1);
                    operator.sortChildren(operatorParamsList,lineParamsList);
                }
            }
        }
    }

}
