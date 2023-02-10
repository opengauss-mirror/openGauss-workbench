package com.nctigba.observability.log.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gitee.starblues.core.PluginExtensionInfo;

@Component
public class PluginExtensionInfoConfig implements PluginExtensionInfo {
	@Override
	public Map<String, Object> extensionInfo() {
		Map<String, Object> map = new HashMap<>();
		map.put("logo",
				"PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJmZWF0aGVyIGZlYXRoZXItZGF0YWJhc2UiPjxlbGxpcHNlIGN4PSIxMiIgY3k9IjUiIHJ4PSI5IiByeT0iMyI+PC9lbGxpcHNlPjxwYXRoIGQ9Ik0yMSAxMmMwIDEuNjYtNCAzLTkgM3MtOS0xLjM0LTktMyI+PC9wYXRoPjxwYXRoIGQ9Ik0zIDV2MTRjMCAxLjY2IDQgMyA5IDNzOS0xLjM0IDktM1Y1Ij48L3BhdGg+PC9zdmc+");
		map.put("theme", "light");
		map.put("pluginType", 1);
		map.put("isNeedConfigured", 0);
		map.put("configAttrs",
				"[{\"attrCode\":\"esHost\",\"attrLabel\":\"ES服务器\"},{\"attrCode\":\"esPort\",\"attrLabel\":\"端口\"}]");
		return map;
	}
}