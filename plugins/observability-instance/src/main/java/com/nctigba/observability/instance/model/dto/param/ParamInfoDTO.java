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
 *  ParamInfoDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/dto/param/ParamInfoDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.param;

import cn.hutool.core.bean.BeanUtil;
import com.nctigba.observability.instance.model.entity.ParamInfoDO;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.exception.CustomException;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Data
@NoArgsConstructor
public class ParamInfoDTO {
    private Integer id;
    private String paramType;
    private String paramName;
    private String actualValue;
    private String suggestValue;
    private String defaultValue;
    private boolean isSuggest;

    public ParamInfoDTO(ParamInfoDO info, String actualValue) {
        BeanUtil.copyProperties(info, this);
        if (info.getDiagnosisRule() == null || "".equals(info.getDiagnosisRule())) {
            this.isSuggest = false;
        } else {
            var manager = new ScriptEngineManager();
            var t = manager.getEngineByName("javascript");
            var bindings = t.createBindings();
            bindings.put("actualValue", actualValue);
            Object object;
            try {
                object = t.eval(info.getDiagnosisRule(), bindings);
            } catch (ScriptException e) {
                throw new CustomException("error", e);
            }
            this.isSuggest = object == null || !"true".equals(object.toString());
        }
        this.actualValue = actualValue;
    }

    public String getParamDetail() {
        return MessageSourceUtils.get(paramType + "." + paramName + ".paramDetail", "");
    }

    public String getUnit() {
        return MessageSourceUtils.get(paramType + "." + paramName + ".unit", "");
    }

    public String getSuggestExplain() {
        return MessageSourceUtils.get(paramType + "." + paramName + ".suggestExplain", "");
    }

    /**
     * Get parameterCategory value
     *
     * @return String
     */
    public String getParameterCategory() {
        return MessageSourceUtils.get(paramType + "." + paramName + ".parameterCategory", "");
    }

    /**
     * Get valueRange value
     *
     * @return String
     */
    public String getValueRange() {
        return MessageSourceUtils.get(paramType + "." + paramName + ".valueRange", "");
    }
}