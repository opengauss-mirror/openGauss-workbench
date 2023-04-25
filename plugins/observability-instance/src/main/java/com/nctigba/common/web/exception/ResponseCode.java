package com.nctigba.common.web.exception;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @description: error code
 * @param:
 * @return:
 * @author: xielibo
 * @date: 2020/11/2
 */
public enum ResponseCode {

	SUCCESS(200, "200"),

	BAD_REQUEST(400, "400"),

	UNAUTHORIZED(401, "401"),

	FORBIDDEN(403, "403"),

	ERROR(500, "500");

	private static Map<Integer, ResponseCode> codeMap = new HashMap<Integer, ResponseCode>();

	private Integer code;

	private String msg;

	private String value;

	/**
	 *
	 */
	private ResponseCode() {
	}

	/**
	 * @param value
	 */
	private ResponseCode(String value) {
		this.value = value;
	}

	/**
	 * @param code
	 * @param msg
	 */
	private ResponseCode(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * @return
	 */
	public Integer code() {
		return this.code;
	}

	/**
	 * @return
	 */
	public String msg() {
		return this.msg;
	}

	/**
	 * @return
	 */
	public String value() {
		return this.value;
	}

	/**
	 * @param codeValue
	 * @return
	 */
	public static ResponseCode getInstance(Integer codeValue) {
		return getCodeMap().get(codeValue);
	}

	/**
	 * @return
	 */
	private static Map<Integer, ResponseCode> getCodeMap() {
		if (codeMap == null || codeMap.size() == 0) {
			codeMap=new HashMap<>();
			ResponseCode[] codeList = ResponseCode.values();
			for (ResponseCode c : codeList) {
				codeMap.put(c.code(), c);
			}
		}
		return codeMap;
	}
}