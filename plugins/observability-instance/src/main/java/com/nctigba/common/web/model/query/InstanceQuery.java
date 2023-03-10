package com.nctigba.common.web.model.query;

import lombok.Data;

/**
 * @description: for search data for a cluster or a node
 * @author: YangZiHao
 * @date: 2022/11/22 8:35
 */

@Data
public class InstanceQuery {
	private String clusterId;
	private String nodeId;
}