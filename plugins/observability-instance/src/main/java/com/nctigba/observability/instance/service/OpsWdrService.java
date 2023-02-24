package com.nctigba.observability.instance.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.entity.OpsWdrEntity;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrScopeEnum;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrTypeEnum;
import com.nctigba.observability.instance.mapper.OpsWdrMapper;
import com.nctigba.observability.instance.model.WdrGeneratorBody;
import com.nctigba.observability.instance.model.WdrSnapshotVO;
import com.nctigba.observability.instance.util.JschUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lhf
 * @date 2022/10/13 15:14
 **/
@Slf4j
@Service
public class OpsWdrService extends ServiceImpl<OpsWdrMapper, OpsWdrEntity> {
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	private IOpsClusterService opsClusterService;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	private IOpsClusterNodeService opsClusterNodeService;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	private HostFacade hostFacade;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	private HostUserFacade hostUserFacade;
	@Autowired
	private JschUtil jschUtil;
	@Autowired
	private ClusterOpsProviderManager clusterOpsProviderManager;
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	protected EncryptionUtils encryptionUtils;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<OpsWdrEntity> listWdr(Page page, String clusterId, WdrScopeEnum wdrScope, WdrTypeEnum wdrType,
			String hostId, Date start, Date end) {
		var wrapper = Wrappers.lambdaQuery(OpsWdrEntity.class).eq(OpsWdrEntity::getClusterId, clusterId)
				.eq(Objects.nonNull(wdrScope), OpsWdrEntity::getScope,
						Objects.nonNull(wdrScope) ? wdrScope.name() : StrUtil.EMPTY)
				.eq(Objects.nonNull(wdrType), OpsWdrEntity::getReportType,
						Objects.nonNull(wdrType) ? wdrType.name() : StrUtil.EMPTY)
				.eq(StrUtil.isNotEmpty(hostId), OpsWdrEntity::getHostId, hostId)
				.ge(Objects.nonNull(start), OpsWdrEntity::getReportAt, start)
				.le(Objects.nonNull(end), OpsWdrEntity::getReportAt, end);
		page.setTotal(getBaseMapper().selectCount(wrapper));
		page.setRecords(getBaseMapper().selectList(wrapper.orderByDesc(OpsWdrEntity::getCreateTime)
				.last(" limit " + (page.getCurrent() - 1) * page.getSize() + "," + page.getSize())));
		return page;
	}

	@Transactional(rollbackFor = Exception.class)
	public void generate(WdrGeneratorBody wdrGeneratorBody) {
		String clusterId = wdrGeneratorBody.getClusterId();
		OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);

		if (Objects.isNull(clusterEntity)) {
			throw new OpsException("Cluster information does not exist");
		}

		List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
		if (CollUtil.isEmpty(opsClusterNodeEntities)) {
			throw new OpsException("Cluster node information does not exist");
		}

		WdrScopeEnum scope = wdrGeneratorBody.getScope();
		if (WdrScopeEnum.CLUSTER == scope) {
			generateClusterWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(),
					wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
		} else {
			generateNodeWdr(clusterEntity, opsClusterNodeEntities, wdrGeneratorBody.getType(),
					wdrGeneratorBody.getHostId(), wdrGeneratorBody.getStartId(), wdrGeneratorBody.getEndId());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Page listSnapshot(Page page, String clusterId, String hostId) {
		var connection = clusterManager.getConnectionByClusterHost(clusterId, hostId);
		String sqlCount = "select count(*) from snapshot.snapshot";
		try (var statement = connection.createStatement(); var rs = statement.executeQuery(sqlCount);) {
			rs.next();
			page.setTotal(rs.getLong(1));
		} catch (Exception e) {
			log.error("Query snapshot record exception", e);
		}
		String sql = "select * from snapshot.snapshot";
		var orderby = ServletUtils.getParameter("orderby");
		if (StringUtils.isNotBlank(orderby))
			sql += " order by " + orderby;
		sql += " limit " + (page.getCurrent() - 1) * page.getSize() + "," + page.getSize();
		var res = new ArrayList<>();
		try (var statement = connection.createStatement(); var rs = statement.executeQuery(sql);) {
			while (rs.next()) {
				var vo = new WdrSnapshotVO();
				vo.setSnapshotId(rs.getInt("snapshot_id"));
				vo.setStartTs(rs.getDate("start_ts"));
				vo.setEndTs(rs.getDate("end_ts"));
				res.add(vo);
			}
		} catch (Exception e) {
			log.error("Query snapshot record exception", e);
		}
		page.setRecords(res);
		return page;
	}

	public void createSnapshot(String clusterId, String hostId) {
		OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);
		if (Objects.isNull(clusterEntity)) {
			throw new OpsException("Cluster information does not exist");
		}

		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (Objects.isNull(hostEntity)) {
			throw new OpsException("host information does not exist");
		}

		List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
		if (CollUtil.isEmpty(opsClusterNodeEntities)) {
			throw new OpsException("Cluster node information is empty");
		}

		OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
				.filter(node -> node.getHostId().equals(hostId)).findFirst()
				.orElseThrow(() -> new OpsException("Cluster node configuration not found"));

		String installUserId = nodeEntity.getInstallUserId();
		OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
		if (Objects.isNull(userEntity)) {
			throw new OpsException("Installation user information does not exist");
		}

		var session = jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
						decrypt(userEntity.getPassword()))
				.orElseThrow(() -> new OpsException("Failed to establish session with host"));

		try {
			clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
					.orElseThrow(() -> new OpsException("The current version does not support"))
					.enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
			String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN,
					String.valueOf(clusterEntity.getPort()));
			Map<String, List<String>> response = new HashMap<>();
			List<String> responseList = new ArrayList<>();
			String sql = "select create_wdr_snapshot();\n\n\\q";

			responseList.add(sql);

			response.put("openGauss=#", responseList);
			JschResult jschResult = jschUtil.executeCommandWithSerialResponse(clientLoginOpenGauss, session, response, null);
			if (0 != jschResult.getExitCode()) {
				log.error("Generate wdr snapshot exception, exit code: {}, log: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException("Generate wdr snapshot exception");
			}

		} catch (Exception e) {
			log.error("Generate wdr snapshot exception", e);
			throw new OpsException("Generate wdr snapshot exception");
		} finally {
			if (Objects.nonNull(session) && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private String decrypt(String password) {
		return encryptionUtils.decrypt(password);
	}

	@Transactional(rollbackFor = Exception.class)
	public void del(String id) {
		OpsWdrEntity wdrEntity = getById(id);
		if (Objects.isNull(wdrEntity)) {
			throw new OpsException("The record to delete does not exist");
		}

		String hostId = wdrEntity.getHostId();
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (Objects.isNull(hostEntity)) {
			throw new OpsException("host information does not exist");
		}

		List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(hostId);
		if (CollUtil.isEmpty(hostUserList)) {
			throw new OpsException("Host user information does not exist");
		}

		OpsHostUserEntity installUser = hostUserList.stream()
				.filter(userEntity -> !"root".equals(userEntity.getUsername())).findFirst()
				.orElseThrow(() -> new OpsException("No installation user information found"));

		Session session = jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUser.getUsername(),
						decrypt(installUser.getPassword()))
				.orElseThrow(() -> new OpsException("Failed to establish connection with host"));

		try {
			rmFile(session, wdrEntity.getReportPath(), wdrEntity.getReportName());
		} finally {
			if (Objects.nonNull(session) && session.isConnected()) {
				session.disconnect();
			}
		}

		removeById(id);
	}

	public void downloadWdr(String wdrId, HttpServletResponse response) {
		OpsWdrEntity wdrEntity = getById(wdrId);
		if (Objects.isNull(wdrEntity)) {
			throw new OpsException("wdr information not found");
		}

		String hostId = wdrEntity.getHostId();
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (Objects.isNull(hostEntity)) {
			throw new OpsException("host information not found");
		}

		OpsHostUserEntity hostUserEntity = hostUserFacade.getById(wdrEntity.getUserId());
		if (Objects.isNull(hostUserEntity)) {
			throw new OpsException("No user information was found to generate the report");
		}

		Session session = jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(),
						decrypt(hostUserEntity.getPassword()))
				.orElseThrow(() -> new OpsException("user failed to establish connection"));

		try {
			jschUtil.download(session, wdrEntity.getReportPath(), wdrEntity.getReportName(), response);
		} finally {
			if (Objects.nonNull(session) && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private void rmFile(Session session, String reportPath, String reportName) {
		String command = MessageFormat.format(SshCommandConstants.DEL_FILE, reportPath + "/" + reportName);
		try {
			JschResult jschResult = jschUtil.executeCommand(command, session);
			if (0 != jschResult.getExitCode()) {
				log.error("delete wdr failed，code:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
			}
		} catch (Exception e) {
			log.error("delete wdr failed");
		}
	}

	private void generateNodeWdr(OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities,
			WdrTypeEnum type, String hostId, String startId, String endId) {
		OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
				.filter(node -> node.getHostId().equals(hostId)).findFirst()
				.orElseThrow(() -> new OpsException("Cluster node configuration not found"));
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (Objects.isNull(hostEntity)) {
			throw new OpsException("host information does not exist");
		}

		String installUserId = nodeEntity.getInstallUserId();
		OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
		if (Objects.isNull(userEntity)) {
			throw new OpsException("Installation user information does not exist");
		}

		Session session = jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
						decrypt(userEntity.getPassword()))
				.orElseThrow(() -> new OpsException("Failed to establish session with host"));
		try {
			clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
					.orElseThrow(() -> new OpsException("The current version does not support"))
					.enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);
			String wdrPath = "/home/" + userEntity.getUsername();
			String wdrName = "WDR-" + StrUtil.uuid() + ".html";
			doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.CLUSTER, type, session, clusterEntity.getPort());
			OpsWdrEntity opsWdrEntity = new OpsWdrEntity();
			opsWdrEntity.setScope(WdrScopeEnum.CLUSTER);
			opsWdrEntity.setReportAt(new Date());
			opsWdrEntity.setReportType(type);
			opsWdrEntity.setReportName(wdrName);
			opsWdrEntity.setReportPath(wdrPath);
			opsWdrEntity.setStartSnapshotId(startId);
			opsWdrEntity.setEndSnapshotId(endId);
			opsWdrEntity.setClusterId(clusterEntity.getClusterId());
			opsWdrEntity.setUserId(userEntity.getHostUserId());
			save(opsWdrEntity);
		} finally {
			if (Objects.nonNull(session) && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private void generateClusterWdr(OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities,
			WdrTypeEnum type, String startId, String endId) {
		OpsClusterNodeEntity masterNodeEntity = opsClusterNodeEntities.stream()
				.filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst()
				.orElseThrow(() -> new OpsException("Cluster master configuration not found"));
		String hostId = masterNodeEntity.getHostId();
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (Objects.isNull(hostEntity)) {
			throw new OpsException("host information does not exist");
		}

		String installUserId = masterNodeEntity.getInstallUserId();
		OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
		if (Objects.isNull(userEntity)) {
			throw new OpsException("Installation user information does not exist");
		}

		Session session = jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
						decrypt(userEntity.getPassword()))
				.orElseThrow(() -> new OpsException("Failed to establish session with host"));

		try {
			clusterOpsProviderManager.provider(clusterEntity.getVersion(), null)
					.orElseThrow(() -> new OpsException("The current version does not support"))
					.enableWdrSnapshot(session, clusterEntity, opsClusterNodeEntities, WdrScopeEnum.CLUSTER, null);

			String wdrPath = "/home/" + userEntity.getUsername();
			String wdrName = "WDR-" + StrUtil.uuid() + ".html";
			doGenerate(wdrPath, wdrName, startId, endId, WdrScopeEnum.CLUSTER, type, session, clusterEntity.getPort());

			OpsWdrEntity opsWdrEntity = new OpsWdrEntity();
			opsWdrEntity.setScope(WdrScopeEnum.CLUSTER);
			opsWdrEntity.setHostId(masterNodeEntity.getHostId());
			opsWdrEntity.setReportAt(new Date());
			opsWdrEntity.setReportType(type);
			opsWdrEntity.setReportName(wdrName);
			opsWdrEntity.setReportPath(wdrPath);
			opsWdrEntity.setStartSnapshotId(startId);
			opsWdrEntity.setEndSnapshotId(endId);
			opsWdrEntity.setClusterId(clusterEntity.getClusterId());
			opsWdrEntity.setUserId(userEntity.getHostUserId());
			save(opsWdrEntity);
		} finally {
			if (Objects.nonNull(session) && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private void doGenerate(String wdrPath, String wdrName, String startId, String endId, WdrScopeEnum scope,
			WdrTypeEnum type, Session session, Integer port) {
		String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
		try {
			Map<String, List<String>> response = new HashMap<>();
			List<String> responseList = new ArrayList<>();
			String startSql = "\\a \\t \\o " + wdrPath + "/" + wdrName + "\n";
			String generateSql = "select generate_wdr_report('" + startId + "', '" + endId + "', '"
					+ type.name().toLowerCase() + "', '" + scope.name().toLowerCase() + "'); \n";
			String endSql = "\\o \\a \\t \n \\q";

			responseList.add(startSql);
			responseList.add(generateSql);
			responseList.add(endSql);

			response.put("openGauss=#", responseList);
			JschResult jschResult = jschUtil.executeCommandWithSerialResponse(clientLoginOpenGauss, session, response, null);
			if (0 != jschResult.getExitCode()) {
				log.error("Generated wdr exception, exit code: {}, log: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException("generate wdr exception");
			}

		} catch (Exception e) {
			log.error("generate wdr exception", e);
			throw new OpsException("generate wdr exception");
		}
	}
}
