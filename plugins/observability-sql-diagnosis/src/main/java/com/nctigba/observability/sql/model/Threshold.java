package com.nctigba.observability.sql.model;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.common.mybatis.JacksonJsonWithClassTypeHandler;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.dto.ThresholdDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "Threshold", autoResultMap = true)
public class Threshold {
	@TableId
	private GrabType knowledgeKey;
	@TableField(typeHandler = JacksonJsonWithClassTypeHandler.class)
	private List<Map<String, Object>> v;

	public static Threshold from(ThresholdDTO<Map<String, Object>> t) {
		return new Threshold(t.getKnowledgeKey(), t.getValues());
	}
}