package com.nctigba.observability.instance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "param_info", autoResultMap = true)
public class ParamInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("paramType")
    String paramType;
    @TableField("paramName")
    String paramName;
    @TableField("paramDetail")
    String paramDetail;
    @TableField("suggestValue")
    String suggestValue;
    @TableField("defaultValue")
    String defaultValue;
    @TableField("unit")
    String unit;
    @TableField("suggestExplain")
    String suggestExplain;
    @TableField("diagnosisRule")
    String diagnosisRule;

}
