package com.nctigba.observability.instance.dto.param;

public class OsParamDTO {
    private String seqNo;
    private String classify;
    private String paramName;
    private String paramDetail;
    private String actualValue;
    private String suggestValue;
    private String defaultValue;
    private String unit;
    private String suggestExplain;

    public OsParamDTO() {
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
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

    public String getUnit() {
        return unit;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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
