package com.nctigba.observability.sql.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TimeConfigDO
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Data
@TableName("time_config")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TimeConfigDO {
    private Integer id;
    private Integer peroid;
    private Integer frequency;
}
