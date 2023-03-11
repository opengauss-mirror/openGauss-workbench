package com.nctigba.observability.sql.model;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnv {
	@TableId(type = IdType.ASSIGN_UUID)
	String id;
	String hostid;
	type type;
	String username;
	String path;
	Integer port;
	@TableField(exist = false)
	OpsHostEntity host;

	public enum type {
		PROMETHEUS,NODE_EXPORTER,OPENGAUSS_EXPORTER,
		ELASTICSEARCH,FILEBEAT,
		AGENT,
		PROMETHEUS_PKG,NODE_EXPORTER_PKG,OPENGAUSS_EXPORTER_PKG,
		ELASTICSEARCH_PKG,FILEBEAT_PKG,
		AGENT_PKG,
	}
}