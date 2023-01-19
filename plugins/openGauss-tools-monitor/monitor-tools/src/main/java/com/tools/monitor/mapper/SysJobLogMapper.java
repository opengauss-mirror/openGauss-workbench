package com.tools.monitor.mapper;

import com.tools.monitor.quartz.domain.SysJobLog;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SysJobLogMapper
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class SysJobLogMapper {
    /**
     * selectJobLogList
     *
     * @param jobLog
     * @return
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return null;
    }

    /**
     * selectJobLogAll
     *
     * @return list
     */
    public List<SysJobLog> selectJobLogAll() {
        return null;
    }

    /**
     * selectJobLogById
     *
     * @param jobLogId
     * @return SysJobLog
     */
    public SysJobLog selectJobLogById(Long jobLogId) {
        return null;
    }

    /**
     * insertJobLog
     *
     * @param jobLog
     * @return int
     */
    public int insertJobLog(SysJobLog jobLog) {
        return 1;
    }

    /**
     * deleteJobLogByIds
     *
     * @param logIds
     * @return int
     */
    public int deleteJobLogByIds(Long[] logIds) {
        return 1;
    }

    /**
     * deleteJobLogById
     *
     * @param jobId
     * @return int
     */
    public int deleteJobLogById(Long jobId) {
        return 1;
    }

    /**
     * cleanJobLog
     */
    public void cleanJobLog() {
    }
}
