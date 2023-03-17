package com.nctigba.observability.instance.service;

import java.util.ArrayList;
import java.util.List;

import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.entity.ParamInfo.type;
import com.nctigba.observability.instance.entity.ParamValueInfo;
import com.nctigba.observability.instance.mapper.ParamInfoMapper;
import com.nctigba.observability.instance.mapper.ParamValueInfoMapper;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.util.SshSession;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParamInfoService {
	private final ClusterManager opsFacade;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	protected EncryptionUtils encryptionUtils;
	protected final ParamValueInfoMapper paramValueInfoMapper;

	public List<ParamInfoDTO> getParamInfo(ParamQuery paramQuery) {
		if ("1".equals(paramQuery.getIsRefresh())) {
			if (StrUtil.isBlank(paramQuery.getPassword())) {
				return null;
			}
			this.updateOsParamInfo(paramQuery);
			this.updateDatabaseParamInfo(paramQuery);
			paramValueInfoMapper.refresh(paramQuery.getNodeId());
		}
		return paramValueInfoMapper.query(paramQuery.getNodeId());
	}

	private static final String DBSETTINGS = "select name,setting from pg_settings";

	private void updateDatabaseParamInfo(ParamQuery paramQuery) {
		try {
			var conn = opsFacade.getConnectionByNodeId(paramQuery.getNodeId());
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(DBSETTINGS);
			var list = new ArrayList<ParamValueInfo>();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				var paramInfo = ParamInfoMapper.getParamInfo(type.DB, name);
				if(paramInfo == null)
					continue;
				list.add(new ParamValueInfo(paramInfo.getId(), paramQuery.getNodeId(), value));
			}
			update(list, type.DB);
		} catch (Exception e) {
			log.info(e.getMessage(), e);
			throw new InstanceException("connect database fail!", e);
		}

	}

	private void updateOsParamInfo(ParamQuery paramQuery) {
		try {
			var node = opsFacade.getOpsNodeById(paramQuery.getNodeId());
			if (node == null)
				throw new InstanceException("node not found!");
			var ssh = SshSession.connect(node.getPublicIp(), node.getHostPort(), "root",
					encryptionUtils.decrypt(paramQuery.getPassword()));
			String[] values = ssh.execute("sysctl -a").split("\n");
			var list = new ArrayList<ParamValueInfo>();
			for (int n = 0; n < values.length; n++) {
				String name = values[n].substring(0, values[n].lastIndexOf("=")).trim();
				String paramData = values[n].substring(values[n].indexOf("=") + 1).trim();
				var paramInfo = ParamInfoMapper.getParamInfo(type.OS, name);
				if(paramInfo == null)
					continue;
				list.add(new ParamValueInfo(paramInfo.getId(), paramQuery.getNodeId(), paramData));
			}
			update(list, type.OS);
		} catch (Exception e) {
			log.info("", e);
			throw new InstanceException("", e);
		}
	}

	private void update(ArrayList<ParamValueInfo> list, type t) {
		var ids = ParamInfoMapper.getIds(t);
		ParamValueInfoMapper.delBySids(ids);
		ParamValueInfoMapper.insertBatch(list);
	}
}