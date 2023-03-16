/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.observability.log.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteIndexDataTask {
	@Autowired
	private EsLogSearchUtils esLogSearchUtils;

	@Scheduled(cron = "${taskCron.autoDeleteIndexData}")
	public void execute() {
		esLogSearchUtils.deleteLogInfo();
	}
}