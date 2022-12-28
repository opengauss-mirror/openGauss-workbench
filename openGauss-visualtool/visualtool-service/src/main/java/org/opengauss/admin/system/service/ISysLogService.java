package org.opengauss.admin.system.service;

import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * System log interface
 */
public interface ISysLogService {

    public SysLogConfigVo getAllLogConfig();

    //    @PostConstruct
    void init();

    public void saveAllLogConfig(SysLogConfigDto sysLogConfigDto);

    public void saveLogConfig(String key, String value);

    public String getConfigByKey(String key);

    public List<Map<String,Object>> listAllLogFile();

    public File getLogFileByName(String filename);
}
