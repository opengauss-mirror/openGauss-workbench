package com.nctigba.common.web.model.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: for search data for a cluster or a node
 * @author: YangZiHao
 * @date: 2022/11/22 8:35
 */

@Data
public class InstanceQuery {
	@ApiModelProperty(value = "cluster ID", required = true)
	private String clusterId;
	@ApiModelProperty(value = "database node ID", required = true)
	private String nodeId;
}