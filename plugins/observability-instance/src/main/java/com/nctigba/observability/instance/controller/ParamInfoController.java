package com.nctigba.observability.instance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.service.ParamInfoService;
import com.nctigba.observability.instance.util.MessageSourceUtil;

import lombok.RequiredArgsConstructor;

/**
 * ParamInfo
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/01/30 15:00
 */
@RestController
@RequestMapping("/observability/v1/param")
@RequiredArgsConstructor
public class ParamInfoController {

	private final ParamInfoService paramInfoService;

	@GetMapping(value = "/paramInfo")
	public List<ParamInfoDTO> paramInfo(ParamQuery paramQuery) {
		MessageSourceUtil.reset();
		if ("".equals(paramQuery.getNodeId()) || paramQuery.getNodeId() == null) {
			throw new InstanceException("nodeId is empty!");
		}
		return paramInfoService.getParamInfo(paramQuery);
	}
}