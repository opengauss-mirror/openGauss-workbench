package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/sqlDiagnosis/api/open/v1/diagnosisTasks")
@RequiredArgsConstructor
public class OpenController {
	private final DiagnosisService diagnosisService;

	@PostMapping(value = "/{id}/result", consumes = "multipart/*")
	public void diagnosisResult(@PathVariable String id, String type, MultipartFile file) throws IOException {
		try {
			diagnosisService.diagnosisResult(id, GrabType.valueOf(type), file);
		}catch (Exception e) {
			throw new CustomException(type, e);
		}
	}
}