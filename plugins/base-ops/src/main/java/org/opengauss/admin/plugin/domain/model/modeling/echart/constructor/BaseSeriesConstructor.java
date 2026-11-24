/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
