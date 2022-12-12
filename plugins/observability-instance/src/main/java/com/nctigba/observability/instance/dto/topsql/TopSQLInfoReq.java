package com.nctigba.observability.instance.dto.topsql;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * TopSQLInfo request dto
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/9/19 11:05
 */
@Data
public class TopSQLInfoReq {
    @ApiModelProperty("instance node info id")
    private String id;
    @ApiModelProperty("TopSQL unique query id")
    private String sqlId;
}
