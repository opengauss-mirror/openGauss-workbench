package com.nctigba.observability.sql.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.mapper.DbMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClusterManager {
	private final DataSource dataSource;
	private final DbMapper dbMapper;

	@Autowired(required = false)
	@AutowiredType(Type.MAIN_PLUGIN)
	private OpsFacade opsFacade;

	public List<String> databaseList(String nodeId) {
		try {
			setCurrentDatasource(nodeId, null);
			return dbMapper.dataBaseList();
		} finally {
			pool();
		}
	}

	/**
	 * set datasource ,need poll after use
	 * 
	 * @see com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder#push(String)
	 * @see com.nctigba.observability.sql.service.ClusterManager#pool()
	 */
	public void setCurrentDatasource(String nodeId, String dbname) {
		if (StringUtils.isBlank(nodeId))
			return;
		var ds = (DynamicRoutingDataSource) dataSource;
		if (ds.getDataSources().containsKey(nodeId)) {
			DynamicDataSourceContextHolder.push(nodeId);
			return;
		}
		var node = getOpsNodeById(nodeId);
		if (node == null)
			return;
		if (StringUtils.isBlank(dbname))
			dbname = node.getDbName();
		var dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("org.opengauss.Driver");
		dataSourceBuilder.url("jdbc:opengauss://" + node.getPublicIp() + ":" + node.getDbPort() + "/" + dbname);
		dataSourceBuilder.username(node.getDbUser());
		dataSourceBuilder.password(node.getDbUserPassword());
		ds.addDataSource(nodeId, dataSourceBuilder.build());
		DynamicDataSourceContextHolder.push(nodeId);
	}

	public void setCurrentDatasource(Task task) {
		setCurrentDatasource(task.getNodeId(), task.getDbName());
	}

	public void pool() {
		DynamicDataSourceContextHolder.poll();
	}

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
			return "0";
		for (OpsClusterVO cluster : getAllOpsCluster()) {
			if (cluster.getClusterNodes().stream().anyMatch(node -> {
				return nodeId.equals(node.getNodeId());
			}))
				return cluster.getClusterId();
		}
		return "unknown";
	}

	@Data
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = true)
	public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
		private String version;

		public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
			BeanUtils.copyProperties(opsClusterNodeVO, this);
			this.version = version;
		}

		public Connection connection() throws SQLException {
			var conn = DriverManager.getConnection(
					"jdbc:opengauss://" + getPublicIp() + ":" + getDbPort() + "/" + getDbName() + "?TimeZone=UTC",
					getDbUser(), getDbUserPassword());
			try (var preparedStatement = conn.prepareStatement("select 1");
					var rs = preparedStatement.executeQuery();) {
				return conn;
			} finally {
			}
		}
	}

	/**
	 * 直接获取指定节点的连接
	 */
	public Connection getConnectionByNodeId(String nodeId) throws SQLException {
		return getOpsNodeById(nodeId).connection();
	}

	/**
	 * 获取指定节点信息
	 */
	public OpsClusterNodeVOSub getOpsNodeById(String nodeId) {
		List<OpsClusterVO> opsClusterVOList = getAllOpsCluster();
		if (CollectionUtils.isEmpty(opsClusterVOList))
			return null;
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
		return null;
	}

	@Value("${spring.profiles.active}")
	private String env;
}