package com.nctigba.observability.instance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "param_value_info", autoResultMap = true)
public class ParamValueInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("sid")
    Integer sid;
    @TableField("instance")
    String instance;
    @TableField("actualValue")
    String actualValue;
}
