package com.tools.monitor.service.impl;

import com.tools.monitor.mapper.SysJobLogMapper;
import com.tools.monitor.quartz.domain.SysJobLog;
import com.tools.monitor.service.ISysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SysJobLogServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService {
    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     * selectJobLogList
     *
     * @param jobLog
     * @return
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * selectJobLogById
     *
     * @param jobLogId
     * @return
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * addJobLog
     *
     * @param jobLog
     */
    @Override
    public void addJobLog(SysJobLog jobLog) {
        jobLogMapper.insertJobLog(jobLog);
    }

    /**
     * deleteJobLogByIds
     *
     * @param logIds logIds
     * @return
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds) {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     * deleteJobLogById
     *
     * @param jobId
     * @return
     */
    @Override
    public int deleteJobLogById(Long jobId) {
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     * cleanJobLog
     */
    @Override
    public void cleanJobLog() {
        jobLogMapper.cleanJobLog();
    }
}
