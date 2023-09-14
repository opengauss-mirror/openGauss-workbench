/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.param;

import cn.hutool.core.bean.BeanUtil;
import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.util.MessageSourceUtil;
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

    public ParamInfoDTO(ParamInfo info, String actualValue) {
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
        return MessageSourceUtil.get(paramType + "." + paramName + ".paramDetail", "");
    }

    public String getUnit() {
        return MessageSourceUtil.get(paramType + "." + paramName + ".unit", "");
    }

    public String getSuggestExplain() {
        return MessageSourceUtil.get(paramType + "." + paramName + ".suggestExplain", "");
    }

    /**
     * Get parameterCategory value
     *
     * @return String
     */
    public String getParameterCategory() {
        return MessageSourceUtil.get(paramType + "." + paramName + ".parameterCategory", "");
    }

    /**
     * Get valueRange value
     *
     * @return String
     */
    public String getValueRange() {
        return MessageSourceUtil.get(paramType + "." + paramName + ".valueRange", "");
    }
}