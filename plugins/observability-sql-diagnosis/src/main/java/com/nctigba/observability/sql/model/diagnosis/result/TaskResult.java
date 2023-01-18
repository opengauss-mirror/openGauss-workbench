package com.nctigba.observability.sql.model.diagnosis.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.common.mybatis.JacksonJsonWithClassTypeHandler;
import com.nctigba.observability.sql.model.diagnosis.Task;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@TableName(value = "diagnosis_task_result", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class TaskResult {
	@TableId
	Integer taskid;
	@TableField("resultState")
	ResultState state;
	@TableField("resultType")
	ResultType resultType;
	@TableField("frameType")
	FrameType frameType;
	Frame.bearing bearing;
	@TableField(typeHandler = JacksonJsonWithClassTypeHandler.class)
	JSON data;

	public enum ResultState {
		NoAdvice,
		Suggestions,
	}

	public TaskResult setData(Object obj) {
		this.data = (JSON) JSONObject.toJSON(obj);
		return this;
	}

	public Frame toFrame() {
		return new Frame().setType(frameType).setData(data);
	}

	public TaskResult(Task task, ResultState state, ResultType resultType, FrameType frameType, Frame.bearing bearing) {
		super();
		this.state = state;
		this.taskid = task.getId();
		this.resultType = resultType;
		this.frameType = frameType;
		this.bearing = bearing;
	}
}