package com.nctigba.observability.sql.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import com.nctigba.observability.sql.mapper.ThresholdMapper;
import com.nctigba.observability.sql.model.Threshold;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.dto.ThresholdDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThresholdService {
	private final ThresholdMapper thresholdMapper;

	public ThresholdDTO<?> get(GrabType key) {
		var threshold = thresholdMapper.selectById(key);
		List<Map<String, Object>> values = threshold == null ? new ArrayList<>() : threshold.getV();
		var clazz = key.getAnalysis().configClass();
		if (clazz == Map.class) {
			var dto = new ThresholdDTO<Map<String, Object>>();
			dto.setKnowledgeKey(key).setValues(values);
			dto.setThresholdKeys(Arrays.asList());
			return dto;
		} else {
			var dto = new ThresholdDTO<>();
			var list = new ArrayList<>();
			values.forEach(val -> {
				try {
					Object obj = clazz.getDeclaredConstructor().newInstance();
					BeanMap beanMap = BeanMap.create(obj);
					beanMap.putAll(val);
					list.add(obj);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				}
			});
			dto.setKnowledgeKey(key).setValues(list);
			var f = clazz.getDeclaredFields();
			for (Field field : f) {
				dto.addKey(field.getName());
			}
			return dto;
		}
	}

	public <T> List<T> getValue(GrabType key, Class<T> clazz) {
		var threshold = thresholdMapper.selectById(key);
		if (threshold == null)
			return Arrays.asList();
		var list = new ArrayList<T>();
		threshold.getV().forEach(val -> {
			try {
				T obj = clazz.getDeclaredConstructor().newInstance();
				BeanMap beanMap = BeanMap.create(obj);
				beanMap.putAll(val);
				list.add(obj);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			}
		});
		return list;
	}

	public ThresholdDTO<Map<String, Object>> save(ThresholdDTO<Map<String, Object>> dto) {
		var threshold = Threshold.from(dto);
		if (thresholdMapper.selectById(dto.getKnowledgeKey()) == null)
			thresholdMapper.insert(threshold);
		else
			thresholdMapper.updateById(threshold);
		return dto;
	}
}