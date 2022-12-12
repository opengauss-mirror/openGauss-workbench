package com.nctigba.observability.instance.dto.topsql;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * TopSQLList Request DTO
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 16:09
 */
@Data
public class TopSQLListReq {
    @ApiModelProperty("instance node info id")
    private String id;
    @ApiModelProperty("TopSQL startTime")
    private String startTime;
    @ApiModelProperty("TopSQL finishTime")
    private String finishTime;
    @ApiModelProperty("the field name for desc sort(db_time|cpu_time|execution_time)")
    private String orderField;
}
