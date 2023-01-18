package com.nctigba.observability.sql.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.dto.ThresholdDTO;
import com.nctigba.observability.sql.service.ThresholdService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1/diagnosis/threshold")
@RequiredArgsConstructor
public class ThresholdController {
	private final ThresholdService thresholdService;

	@GetMapping("/{key}")
	public ThresholdDTO<?> get(@PathVariable GrabType key) {
		return thresholdService.get(key);
	}

	@PutMapping("")
	public ThresholdDTO<Map<String, Object>> put(@RequestBody ThresholdDTO<Map<String, Object>> threshold) {
		thresholdService.save(threshold);
		return threshold;
	}
}