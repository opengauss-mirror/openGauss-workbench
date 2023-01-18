package com.nctigba.observability.sql.service.diagnosis;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;

public interface ResultAnalysis {
	void analysis(GrabType grabType, Task task, MultipartFile file);

	default public Class<?> configClass(){
		return Map.class;
	}
}