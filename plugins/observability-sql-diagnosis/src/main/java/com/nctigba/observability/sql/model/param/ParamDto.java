package com.nctigba.observability.sql.model.param;

import lombok.Data;

@Data
public class ParamDto {
    private String title;
    private String paramName;
    private String currentValue;
    private String unit;
    private String paramDescription;
    private String suggestValue;
    private String suggestReason;
}
