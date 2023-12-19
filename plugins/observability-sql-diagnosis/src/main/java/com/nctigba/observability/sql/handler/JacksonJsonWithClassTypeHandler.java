/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  JacksonJsonWithClassTypeHandler.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/handler/JacksonJsonWithClassTypeHandler.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
@MappedJdbcTypes(value = { JdbcType.VARCHAR }, includeNullJdbcType = true)
public class JacksonJsonWithClassTypeHandler<E> extends BaseTypeHandler<E> {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
		MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);
	}
	private final Class<E> type;

	public JacksonJsonWithClassTypeHandler(Class<E> type) {
		Objects.requireNonNull(type);
		this.type = type;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		if (Objects.isNull(parameter)) {
			ps.setString(i, null);
			return;
		}
		ps.setString(i, toJson(parameter));
	}

	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toObject(rs.getString(columnName), type);
	}

	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toObject(rs.getString(columnIndex), type);
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toObject(cs.getString(columnIndex), type);
	}

	private String toJson(E obj) {
		if (Objects.isNull(obj)) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("mybatis column to json error,obj:" + obj, e);
		}
	}

	private E toObject(String json, Class<E> clazz) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		try {
			return MAPPER.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			log.error("mybatis column json to object error,json:{}", json, e);
			return null;
		}
	}
}
