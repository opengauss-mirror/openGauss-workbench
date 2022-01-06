package com.nctigba.observability.sql.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nctigba.common.web.config.ControllerConfig;
import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TreeNode;
import com.nctigba.observability.sql.model.dto.TaskDTO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.service.DiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1/diagnosisTasks")
@RequiredArgsConstructor
public class SQLDiagnosisController extends ControllerConfig {
	private final DiagnosisService diagnosisService;
	private final LocaleString localeToString;

	@PostMapping("")
	public Task start(TaskDTO task) {
		return diagnosisService.start(task.toTask());
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable int id) {
		diagnosisService.delete(id);
	}

	@GetMapping("")
	public Object page(TaskQuery taskQuery) {
		return localeToString.trapLanguage(diagnosisService.page(taskQuery), ObjectNode.class);
	}

	@GetMapping("/{id}")
	public Object get(@PathVariable int id) {
		return localeToString.trapLanguage(diagnosisService.one(id), ObjectNode.class);
	}

	@GetMapping("/{id}/suggestPoints")
	public TreeNode detail(@PathVariable int id, @RequestParam(defaultValue = "true") boolean all) {
		return localeToString.trapLanguage(diagnosisService.detail(id, all));
	}

	@GetMapping("/{id}/suggestions/{type}")
	public Frame result(@PathVariable int id, @PathVariable("type") ResultType type) {
		return localeToString.trapLanguage(diagnosisService.result(id, type));
	}

	private final DiagnosisResourceMapper resourceMapper;

	@GetMapping(value = "/res/{id}.{type}")
	public void res(@PathVariable String id, @PathVariable String type, HttpServletResponse resp) throws IOException {
		switch (type) {
		case "svg":
			resp.setContentType("image/svg+xml");
			break;
		case "png":
			resp.setContentType("image/png");
			break;
		}
		resourceMapper.selectById(id).to(resp.getOutputStream());
	}

	@GetMapping(value = "/plan/{nodeId}/{sqlId}")
	public AppResult plan(@PathVariable String nodeId, @PathVariable String sqlId) {
		return AppResult.ok("success").addData(diagnosisService.plan(nodeId, sqlId));
	}
}