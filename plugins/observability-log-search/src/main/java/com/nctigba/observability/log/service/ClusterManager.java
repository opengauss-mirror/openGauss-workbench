package com.nctigba.observability.log.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClusterManager {
	@Autowired(required = false)
	@AutowiredType(Type.MAIN_PLUGIN)
	private OpsFacade opsFacade;

	/**
	 * Get all cluster information
	 */
	public List<OpsClusterVO> getAllOpsCluster() {
		List<OpsClusterVO> opsClusterVOList = new ArrayList<>();
		try {
			if (opsFacade != null)
				opsClusterVOList = opsFacade.listCluster();
		} catch (Exception e) {
			log.info("get all ops cluster fail:{}", e.getMessage());
		}
		return opsClusterVOList;
	}

	public String getOpsClusterIdByNodeId(String nodeId) {
		if ("0".equals(nodeId))
			throw new RuntimeException("node not found");
		for (OpsClusterVO cluster : getAllOpsCluster()) {
			if (cluster.getClusterNodes().stream().anyMatch(node -> {
				return nodeId.equals(node.getNodeId());
			}))
				return cluster.getClusterId();
		}
		throw new RuntimeException("node not found");
	}

	@Data
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = true)
	public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
		private String version;

		@Override
		public Integer getDbPort() {
			if ("1584444406327418882".equals(getNodeId()))
				return 9190;
			return super.getDbPort();
		}

		public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
			BeanUtils.copyProperties(opsClusterNodeVO, this);
			this.version = version;
		}

		public Connection connection() throws SQLException {
			var conn = DriverManager.getConnection(
					"jdbc:opengauss://" + getPublicIp() + ":" + getDbPort() + "/" + getDbName(), getDbUser(),
					getDbUserPassword());
			try (var preparedStatement = conn.prepareStatement("select 1");
					var rs = preparedStatement.executeQuery();) {
				return conn;
			} catch (Exception e) {
				log.error("test connection fail:{}", e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * Directly obtain the connection of the specified node
	 */
	public Connection getConnectionByNodeId(String nodeId) throws SQLException {
		return getOpsNodeById(nodeId).connection();
	}

	/**
	 * Get the specified node information
	 */
	public OpsClusterNodeVOSub getOpsNodeById(String nodeId) {
		List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
		if (CollectionUtils.isEmpty(opsClusterVOList))
			throw new RuntimeException("node not found");
		for (OpsClusterVO cluster : opsClusterVOList) {
			List<OpsClusterNodeVO> nodes = cluster.getClusterNodes();
			if (CollectionUtils.isEmpty(nodes))
				continue;
			for (OpsClusterNodeVO clusterNode : nodes) {
				if (nodeId.equals(clusterNode.getNodeId())) {
					return new OpsClusterNodeVOSub(clusterNode, cluster.getVersion());
				}
			}
		}
		throw new RuntimeException("node not found");
	}

	/**
	 * Get the specified node information
	 */
	public OpsClusterVO getOpsClusterByNodeId(String nodeId) {
		List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
		if (CollectionUtils.isEmpty(opsClusterVOList))
			throw new RuntimeException("cluster not found");
		for (OpsClusterVO cluster : opsClusterVOList) {
			List<OpsClusterNodeVO> nodes = cluster.getClusterNodes();
			if (CollectionUtils.isEmpty(nodes))
				continue;
			for (OpsClusterNodeVO clusterNode : nodes) {
				if (nodeId.equals(clusterNode.getNodeId())) {
					return cluster;
				}
			}
		}
		throw new RuntimeException("cluster not found");
	}
}