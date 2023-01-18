package com.nctigba.observability.sql.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ThresholdDTO<T> {
	private GrabType knowledgeKey;
	private List<key> thresholdKeys = new ArrayList<>();

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	public class key {
		private String keyName;
	}

	private List<T> values;

	public void addKey(String keyName) {
		thresholdKeys.add(new key(keyName));
	}
}