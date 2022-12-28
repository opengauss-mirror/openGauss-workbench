package org.opengauss.admin.system.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.opengauss.admin.common.constant.LogConstants;
import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;
import org.opengauss.admin.system.domain.SysLogConfig;
import org.opengauss.admin.system.mapper.SysLogConfigMapper;
import org.opengauss.admin.system.service.ISysLogService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System log service
 */
@Slf4j
@Service
public class SysLogServiceImpl implements ISysLogService {

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    @Value("${logging.file.path}")
    private String loggingFilePath;

    @Autowired
    private SysLogConfigMapper sysLogConfigMapper;

    /**
     * Override log config from table sys_log_config
     */
    @Override
    public void init() {

        String level = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_LEVEL);
        String maxFileSize = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE);
        String totalSizeCap = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP);
        String maxHistory = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_MAX_HISTORY);
        if(null == level){
            level = LogConstants.DEFAULT_LEVEL;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_LEVEL, LogConstants.DEFAULT_LEVEL);
        }
        if(null == maxHistory){
            maxHistory = LogConstants.DEFAULT_MAX_HISTORY.toString();
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_HISTORY,LogConstants.DEFAULT_MAX_HISTORY.toString());
        }
        if(null == maxFileSize){
            maxFileSize = LogConstants.DEFAULT_MAX_FILE_SIZE;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE, LogConstants.DEFAULT_MAX_FILE_SIZE);
        }
        if(null == totalSizeCap){
            totalSizeCap = LogConstants.DEFAULT_TOTAL_SIZE_CAP;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP, totalSizeCap);
        }

        changeLogLevel(level);
        changeLogAppender(totalSizeCap,maxFileSize,Integer.parseInt(maxHistory));
    }


    /**
     * Change logback print level
     * @param level the log level
     */
    public void changeLogLevel(String level) {
        List<Logger> loggers = loggerContext.getLoggerList();
        Level parsedLevel = Level.toLevel(level);
        for(Logger logger : loggers){
            logger.setLevel(parsedLevel);
        }
    }

    /**
     * Change default logback appender
     * @param totalSizeCap
     * @param maxFileSize
     * @param maxHistory
     */
    public void changeLogAppender(String totalSizeCap, String maxFileSize, Integer maxHistory) {
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>) rootLogger.getAppender(LogConstants.DEFAULT_APPENDER);
        SizeAndTimeBasedRollingPolicy policy = (SizeAndTimeBasedRollingPolicy) appender.getRollingPolicy();
        policy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        policy.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
        policy.setMaxHistory(maxHistory);
        policy.start();
        appender.start();
    }

    /**
     * Save and apply all log config
     * @param dto
     */
    public void saveAllLogConfig(SysLogConfigDto dto) {
        changeLogLevel(dto.getLevel());
        saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_LEVEL, dto.getLevel());
        changeLogAppender(dto.getTotalSizeCap(), dto.getMaxFileSize(), dto.getMaxHistory());
        saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE, dto.getMaxFileSize());
        saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP, dto.getTotalSizeCap());
        saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_HISTORY, dto.getMaxHistory().toString());
    }

    /**
     * Get all log config
     * @return SysLogConfigVo
     */
    public SysLogConfigVo getAllLogConfig() {
        String level = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_LEVEL);
        String maxFileSize = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE);
        String totalSizeCap = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP);
        String maxHistory = getConfigByKey(LogConstants.SYS_LOG_CONFIG_KEY_MAX_HISTORY);
        SysLogConfigVo sysLogConfigVo = new SysLogConfigVo();
        sysLogConfigVo.setLevel(level);
        sysLogConfigVo.setMaxFileSize(maxFileSize);
        sysLogConfigVo.setTotalSizeCap(totalSizeCap);
        if(null != maxHistory){
            sysLogConfigVo.setMaxHistory(Integer.parseInt(maxHistory));
        }
        return sysLogConfigVo;
    }

    /**
     * save system config data
     * @param key  the config key
     * @param value the config value
     */
    @Override
    public void saveLogConfig(String key, String value) {
        SysLogConfig config = new SysLogConfig();
        config.setKey(key);
        config.setValue(value);
        String exist = getConfigByKey(key);
        if(null != exist){
            LambdaQueryWrapper<SysLogConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysLogConfig::getKey,key);
            sysLogConfigMapper.update(config, queryWrapper);
        }else {
            sysLogConfigMapper.insert(config);
        }
    }

    /**
     * get the config value by config key
     * @param key the config key
     * @return the config value
     */
    @Override
    public String getConfigByKey(String key) {
        LambdaQueryWrapper<SysLogConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysLogConfig::getKey,key);
        SysLogConfig config = sysLogConfigMapper.selectOne(queryWrapper);
        if(config == null){
            return null;
        }
        return config.getValue();
    }

    /**
     * List all log files in the log directory
     * @return FileList
     */
    @Override
    public List<Map<String, Object>> listAllLogFile() {
        File dir = new File(loggingFilePath);
        File[] childrenFiles = dir.listFiles();
        List<Map<String, Object>> files = new ArrayList<>();
        if(null != childrenFiles){
            for (File childFile : childrenFiles) {
                if (childFile.isFile()) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("name",childFile.getName());
                    file.put("size", childFile.length());
                    try {
                      BasicFileAttributes attrs = Files.readAttributes(childFile.toPath(), BasicFileAttributes.class);
                      file.put("createdAt", attrs.creationTime().toMillis());
                      file.put("updatedAt", attrs.lastModifiedTime().toMillis());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    files.add(file);
                }
            }
        }
        return files;
    }

    /**
     * Get single log file by filename
     * @param filename the filename
     * @return File
     */
    @Override
    public File getLogFileByName(String filename) {
        return new File(loggingFilePath + File.separator + filename);
    }
}
