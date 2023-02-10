package com.nctigba.observability.instance.config;

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
				"PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJmZWF0aGVyIGZlYXRoZXItY29tcGFzcyI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTAiPjwvY2lyY2xlPjxwb2x5Z29uIHBvaW50cz0iMTYuMjQgNy43NiAxNC4xMiAxNC4xMiA3Ljc2IDE2LjI0IDkuODggOS44OCAxNi4yNCA3Ljc2Ij48L3BvbHlnb24+PC9zdmc+");
		map.put("theme", "light");
		map.put("pluginType", 1);
		map.put("isNeedConfigured", 0);
		map.put("configAttrs",
				"[{\"attrCode\":\"esHost\",\"attrLabel\":\"ES服务器\"},{\"attrCode\":\"esPort\",\"attrLabel\":\"端口\"}]");
		return map;
	}
}