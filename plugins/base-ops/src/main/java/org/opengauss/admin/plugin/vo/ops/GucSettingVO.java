package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

@Data
public class GucSettingVO {
    private String name;
    private String value;
    private String unit;
    private String shortDesc;
    private String extraDesc;
    private String varType;
    private String enumVals;
    private String maxVal;
    private String minVal;
    private String defaultVal;
    private String context;
    // if this setting is changed by frontend, set to true
    private Boolean hasChanged;
}
