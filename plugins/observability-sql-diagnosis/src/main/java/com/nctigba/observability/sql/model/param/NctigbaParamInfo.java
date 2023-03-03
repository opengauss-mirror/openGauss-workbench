package com.nctigba.observability.sql.model.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("nctigba_param_info")
public class NctigbaParamInfo {
    @TableField("seq_no")
    String seqNo;
    String classify;
    @TableField("param_name")
    String paramName;
    @TableField("param_detail")
    String paramDetail;
    @TableField("actual_value")
    String actualValue;
    @TableField("suggest_value")
    String suggestValue;
    @TableField("default_value")
    String defaultValue;
    String unit;
    @TableField("suggest_explain")
    String suggestExplain;
    String diagnosis;

}
