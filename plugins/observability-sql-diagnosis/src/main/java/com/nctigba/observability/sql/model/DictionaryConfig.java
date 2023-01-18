package com.nctigba.observability.sql.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@TableName("dictionary_config")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DictionaryConfig {
	@TableId
	String id;
	@TableField("nodeId")
	String nodeId;
	String key;
	String value;

	public String getId() {
		if("0-0".equals(id))
			return this.id;
		return this.id = nodeId + "-" + key;
	}

	public DictionaryConfig(String nodeId, String key, String value) {
		super();
		this.nodeId = nodeId;
		this.key = key;
		this.value = value;
		this.id = nodeId + "-" + key;
	}
}