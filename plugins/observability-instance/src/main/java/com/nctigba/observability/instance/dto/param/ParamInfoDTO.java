package com.nctigba.observability.instance.dto.param;

public class ParamInfoDTO {
    private Integer id;
    private String paramType;
    private String paramName;
    private String paramDetail;
    private String actualValue;
    private String suggestValue;
    private String defaultValue;
    private String unit;
    private String suggestExplain;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamDetail() {
        return paramDetail;
    }

    public void setParamDetail(String paramDetail) {
        this.paramDetail = paramDetail;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getSuggestValue() {
        return suggestValue;
    }

    public void setSuggestValue(String suggestValue) {
        this.suggestValue = suggestValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSuggestExplain() {
        return suggestExplain;
    }

    public void setSuggestExplain(String suggestExplain) {
        this.suggestExplain = suggestExplain;
    }
}
