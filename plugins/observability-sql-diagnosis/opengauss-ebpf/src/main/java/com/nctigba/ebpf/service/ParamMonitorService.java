package com.nctigba.ebpf.service;

/**
 * param service
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/02/03 09:30
 */
public interface ParamMonitorService {
    void getOsParamData(String tid, String taskid, String monitorType);
}
