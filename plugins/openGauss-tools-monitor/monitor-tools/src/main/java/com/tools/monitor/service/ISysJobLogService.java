package com.tools.monitor.service;

import com.tools.monitor.quartz.domain.SysJobLog;

import java.util.List;

/**
 * ISysJobLogService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface ISysJobLogService {

     List<SysJobLog> selectJobLogList(SysJobLog jobLog);

     SysJobLog selectJobLogById(Long jobLogId);

     void addJobLog(SysJobLog jobLog);

     int deleteJobLogByIds(Long[] logIds);

     int deleteJobLogById(Long jobId);

     void cleanJobLog();
}
