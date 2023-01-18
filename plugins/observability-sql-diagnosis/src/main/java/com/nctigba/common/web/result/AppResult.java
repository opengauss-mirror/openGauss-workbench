package com.nctigba.common.web.result;

import java.util.HashMap;

public class AppResult {
	private String code = "200";
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	private String error;
	private String msg;
	private Object data;
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static AppResult ok(String statusMsg) {
		AppResult result = new AppResult();
		result.setCode("200");
		result.setMsg(statusMsg);
		return result;
	}
	
	public static AppResult error(String statusMsg) {
		AppResult result = new AppResult();
		result.setCode("500");
		result.setMsg(statusMsg);
		return result;
	}

	@SuppressWarnings("unchecked")
	public AppResult addData(String key, Object object) {
		if (this.data == null)
			this.data = new HashMap<String, Object>();
		((HashMap<String, Object>) this.data).put(key, object);
		return this;
	}

	public AppResult addData(Object object) {
		this.setData(object);
		return this;
	}

	@SuppressWarnings("unchecked")
	public AppResult addList(Object object) {
		if (this.data == null)
			this.data = new HashMap<String, Object>();
		((HashMap<String, Object>) this.data).put("list", object);
		return this;
	}

	@SuppressWarnings("unchecked")
	public AppResult addTotalCount(Object object) {
		if (this.data == null)
			this.data = new HashMap<String, Object>();
		((HashMap<String, Object>) this.data).put("total", object);
		return this;
	}
}