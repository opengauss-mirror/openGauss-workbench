package com.nctigba.common.web.config;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Date;

public class ControllerConfig {
	@InitBinder // 解决前段传过来时间的字符串解析成时间报错问题
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(DateFormatter.getDateFormatter(), true));
	}
}