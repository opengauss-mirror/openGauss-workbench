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
package org.opengauss.admin.plugin.domain.model.modeling.echart.constructor;

import java.util.Collections;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class BaseSeriesConstructor {

    public void culDescartes(List<List<String>> dimvalue, List<String> result, int layer, String curString)
    {
        if (layer < dimvalue.size() - 1)
        {
            if (dimvalue.get(layer).size() == 0) {
                culDescartes(dimvalue, result, layer + 1, curString);
            }
            else
            {
                for (int i = 0; i < dimvalue.get(layer).size(); i++)
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(curString);
                    stringBuilder.append(dimvalue.get(layer).get(i));
                    culDescartes(dimvalue, result, layer + 1, stringBuilder.toString());
                }
            }
        }
        else if (layer == dimvalue.size() - 1)
        {
            if (dimvalue.get(layer).size() == 0) {
                result.add(curString);
            }
            else
            {
                for (int i = 0; i < dimvalue.get(layer).size(); i++)
                {
                    if (curString.isEmpty()) {
                        result.add(curString + dimvalue.get(layer).get(i));
                    } else {
                        result.add(curString + dimvalue.get(layer).get(i));
                    }
                }
            }
        }
    }

    public Float culSummaryValue(List<Float> oriData, String summaryType) {
        Float result = 0f;
        if (oriData.size() == 0) {
            return result;
        }

        switch (summaryType) {
            case "sum" : {
                for (Float value : oriData ) {
                    result += value;
                }
                break;
            }
            case "min" : {
                result = min(oriData);
                break;
            }
            case "max" : {
                result = max(oriData);
                break;
            }
            case "avg" : {
                result = avg(oriData);
                break;
            }
            case "mid" : {
                result = median(oriData);
                break;
            }
            default: {
                result = oriData.get(0);
                break;
            }
        }

        return result;
    }

    public Float max(List<Float> oriData) {
        return Collections.max(oriData);
    }

    public Float min(List<Float> oriData) {
        return Collections.min(oriData);
    }

    public Float avg(List<Float> oriData) {
        Float sum = 0f;
        for (int i = 0; i < oriData.size(); i++) {
            sum += oriData.get(i);
        }
        return sum / oriData.size();
    }

    public Float median(List<Float> oriData) {
        Collections.sort(oriData);
        if (oriData.size() % 2 == 0) {
            int index = oriData.size() / 2;
            return (Float) ((oriData.get(index - 1) + oriData.get(index))) / 2;
        }
        return oriData.get(oriData.size() / 2);
    }


    public float toFloat(Object value) {
        if (value == null) {
            return 0.0f;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        } else {
            return 0.0f;
        }
    }
}
