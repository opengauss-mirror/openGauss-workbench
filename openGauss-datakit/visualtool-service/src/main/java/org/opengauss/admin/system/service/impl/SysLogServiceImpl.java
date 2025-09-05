/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SysLogServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysLogServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.opengauss.admin.common.constant.LogConstants;
import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;
import org.opengauss.admin.system.domain.SysLogConfig;
import org.opengauss.admin.system.mapper.SysLogConfigMapper;
import org.opengauss.admin.system.service.ISysLogService;

import lombok.extern.slf4j.Slf4j;

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
        if (level == null) {
            level = LogConstants.DEFAULT_LEVEL;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_LEVEL, level);
        }
        if (maxHistory == null) {
            maxHistory = LogConstants.DEFAULT_MAX_HISTORY.toString();
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_HISTORY, maxHistory);
        }
        if (maxFileSize == null) {
            maxFileSize = LogConstants.DEFAULT_MAX_FILE_SIZE;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE, maxFileSize);
        }
        if (totalSizeCap == null) {
            totalSizeCap = LogConstants.DEFAULT_TOTAL_SIZE_CAP;
            saveLogConfig(LogConstants.SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP, totalSizeCap);
        }
        changeLogLevel(level);
        changeLogAppender(totalSizeCap, maxFileSize, Integer.parseInt(maxHistory));
    }

    /**
     * Change logback print level
     *
     * @param level the log level
     */
    public void changeLogLevel(String level) {
        Configurator.setRootLevel(org.apache.logging.log4j.Level.valueOf(level));
        if (LogManager.getContext(false) instanceof LoggerContext ctx) {
            for (LoggerConfig loggerConfig : ctx.getConfiguration().getLoggers().values()) {
                loggerConfig.setLevel(org.apache.logging.log4j.Level.valueOf(level));
            }
            ctx.updateLoggers();
        }
    }

    /**
     * Change default logback appender
     *
     * @param totalSizeCap
     * @param maxFileSize
     * @param maxHistory
     */
    public void changeLogAppender(String totalSizeCap, String maxFileSize, Integer maxHistory) {
        if (LogManager.getContext(false) instanceof LoggerContext ctx) {
            Configuration config = ctx.getConfiguration();
            SizeBasedTriggeringPolicy sizePolicy = SizeBasedTriggeringPolicy.createPolicy(maxFileSize);
            DefaultRolloverStrategy rolloverStrategy = DefaultRolloverStrategy.newBuilder()
                .withMax(Integer.toString(maxHistory))
                .build();
            String filePattern = loggingFilePath + File.separator + "sys-%d{yyyy-MM-dd}-%i.log";
            RollingFileAppender appender = RollingFileAppender.newBuilder()
                .withFileName(loggingFilePath + File.separator + "sys.log")
                .withFilePattern(filePattern)
                .withPolicy(sizePolicy)
                .withStrategy(rolloverStrategy)
                .setLayout(PatternLayout.createDefaultLayout(config))
                .setName("RollingFile")
                .setConfiguration(config)
                .build();
            appender.start();
            config.addAppender(appender);
            LoggerConfig rootLogger = config.getRootLogger();
            rootLogger.addAppender(appender, null, null);
            ctx.updateLoggers();
        }
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
                        log.error("list all log error, message: {}", e.getMessage());
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
