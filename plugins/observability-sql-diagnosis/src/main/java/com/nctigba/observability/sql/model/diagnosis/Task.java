package com.nctigba.observability.sql.model.diagnosis;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.util.LocaleString;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName(value = "diagnosis_task", autoResultMap = true)
@Accessors(chain = true)
public class Task {
	@TableId(type = IdType.AUTO)
	Integer id;
	@TableField("clusterId")
	String clusterId;
	@TableField("nodeId")
	String nodeId;
	@TableField("dbName")
	String dbName;
	@TableField("sqlId")
	String sqlId;
	String name = "new Task";
	String sql;
	@TableField(typeHandler = TypeHander.class)
	config conf = new config();
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	Date starttime;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	Date endtime;
	@TableField(fill = FieldFill.UPDATE)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	Date lasttime = new Date();
	Integer progress;
	@TableField(exist = false)
	String cost;
	TaskState state;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC", shape = JsonFormat.Shape.ANY)
	Date createtime = new Date();
	TaskType tasktype;
	String remarks = "";
	// 分析过程参数存储空间
	@TableField(typeHandler = TypeHander.class)
	datas data = new datas();

	public static class TypeHander extends JacksonTypeHandler {
		public TypeHander(Class<?> type) {
			super(type);
		}

		@Override
		protected Object parse(String json) {
			try {
				return super.parse(json);
			} catch (Exception e) {
				return null;
			}
		}
	}

	public config getConf() {
		if (conf != null)
			return conf;
		return conf = new config();
	}

	public String analySql() {
		if (getConf().explainAnalysis)
			return StringUtils.isBlank(sql) ? "" : "explain analyze " + sql;
		return sql;
	}

	@Data
	public static class datas {
		Integer pid;
		Long debugQueryId;
		Long sessionId;
	}

	public Long waitSessionId() {
		int counter = 0;
		while (counter++ < 10000 && getData().getSessionId() == null)
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e1) {
			}
		if (getData().getSessionId() == 0L)
			throw new CustomException("sessionid catch fail");
		return getData().getSessionId();
	}

	public boolean running() {
		return getState().ordinal() <= TaskState.sqlRunning.ordinal();
	}

	@Data
	public static class config {
		boolean offCpu = false;
		boolean onCpu = false;
		boolean explainAnalysis = false;
	}

	public String getCost() {
		if (this.cost != null)
			return this.cost;
		Long costSec;
		if (endtime != null)
			costSec = endtime.getTime() - starttime.getTime();
		else
			costSec = 0L;
		return this.cost = DateUtil.secondToTime(costSec.intValue() / 1000) + String.format(".%03d", costSec % 1000);
	}

	public Frame toUL() {
		var list = new ArrayList<Map<String, Object>>();
		var map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.id"), id + "");
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.name"), name);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.clusterId"), getClusterId());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.NodeId"), getNodeId());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.DbName"), getDbName());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("Top SQL ID", getSqlId());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.tasktype"), tasktype.getValue());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.state"), state.getValue());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.starttime"), starttime == null ? "" : starttime);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.endtime"), endtime == null ? "" : endtime);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.lasttime"), lasttime);
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.cost"), getCost());
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("sql", StringUtils.defaultIfEmpty(sql, "").replaceAll(System.lineSeparator(), "<br/>"));
		list.add(map);
		map = new HashMap<String, Object>();
		map.put(LocaleString.trapLanguage("Task.remarks"), StringUtils.defaultIfEmpty(remarks, ""));
		list.add(map);
		map = new HashMap<String, Object>();
		return new Frame(FrameType.UL).setData(list);
	}

	public synchronized Task addRemarks(String remark) {
		this.remarks += "<br/>" + LocalTime.now() + " " + remark;
		return this;
	}

	public Task addRemarks(String taskState, Throwable e) {
		StringWriter sw = new StringWriter();
		try (PrintWriter pw = new PrintWriter(sw);) {
			e.printStackTrace(pw);
		}
		String msg = sw.toString().replaceAll(System.lineSeparator(), "<br/>");
		return addRemarks(taskState + "<br/>" + msg);
	}

	public Task addRemarks(TaskState taskState) {
		setState(taskState);
		return addRemarks(taskState.getValue());
	}

	public Task addRemarks(TaskState taskState, Object message) {
		setState(taskState);
		return addRemarks(taskState.getValue() + "[" + message + "]");
	}

	public Task addRemarks(TaskState taskState, Throwable e) {
		setState(taskState);
		return addRemarks(taskState.getValue(), e);
	}
}