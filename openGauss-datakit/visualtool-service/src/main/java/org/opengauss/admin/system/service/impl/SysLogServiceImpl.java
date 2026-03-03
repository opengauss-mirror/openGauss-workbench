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
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.system.domain.SysLogConfig;
import org.opengauss.admin.system.mapper.SysLogConfigMapper;
import org.opengauss.admin.system.mapper.SysMenuMapper;
import org.opengauss.admin.system.mapper.SysUserMapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

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
     * List all log files in the log directory with user permission filter
     *
     * @return FileList
     */
    @Override
    public List<Map<String, Object>> listAllLogFile() {
        // Get current login user
        String username = SecurityUtils.getUsername();

        // Admin user can see all logs
        if ("admin".equals(username)) {
            return listAllLogFilesWithoutPermissionCheck();
        }

        // For non-admin users, apply permission filter
        Set<String> allowedPluginIds = getAllowedPluginIds(username);
        return listLogFilesWithPermissionFilter(allowedPluginIds);
    }

    /**
     * Get allowed plugin IDs for current user
     *
     * @param username current login username
     * @return set of allowed plugin IDs
     */
    private Set<String> getAllowedPluginIds(String username) {
        // Get user information
        SysUser user = getUserByUsername(username);

        // Get user roles
        List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(Long.valueOf(user.getUserId()));

        // Get menus by roles
        List<SysMenu> menus = getMenusByRoleIds(roleIds);

        // Extract plugin IDs from menus
        return extractPluginIdsFromMenus(menus);
    }

    /**
     * Get user by username
     *
     * @param username username
     * @return SysUser object
     */
    private SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getUserName, username);
        return sysUserMapper.selectOne(userQuery);
    }

    /**
     * Get menus by role IDs
     *
     * @param roleIds list of role IDs
     * @return list of menus
     */
    private List<SysMenu> getMenusByRoleIds(List<Long> roleIds) {
        List<SysMenu> menus = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<SysMenu> roleMenus = sysMenuMapper.selectMenusByRoleId(roleId);
            menus.addAll(roleMenus);
        }
        return menus;
    }

    /**
     * Extract plugin IDs from menus
     *
     * @param menus list of menus
     * @return set of plugin IDs
     */
    private Set<String> extractPluginIdsFromMenus(List<SysMenu> menus) {
        Set<String> allowedPluginIds = new HashSet<>();
        for (SysMenu menu : menus) {
            if (menu.getPluginId() != null && !menu.getPluginId().isEmpty()) {
                allowedPluginIds.add(menu.getPluginId());
            }
        }
        return allowedPluginIds;
    }

    /**
     * List log files with permission filter
     *
     * @param allowedPluginIds set of allowed plugin IDs
     * @return list of log files
     */
    private List<Map<String, Object>> listLogFilesWithPermissionFilter(Set<String> allowedPluginIds) {
        File dir = new File(loggingFilePath);
        File[] childrenFiles = dir.listFiles();
        List<Map<String, Object>> files = new ArrayList<>();

        if (childrenFiles != null) {
            for (File childFile : childrenFiles) {
                if (childFile.isFile()) {
                    processLogFile(childFile, files, allowedPluginIds);
                }
            }
        }
        return files;
    }

    /**
     * Process single log file
     *
     * @param file             log file
     * @param files            result list
     * @param allowedPluginIds set of allowed plugin IDs
     */
    private void processLogFile(File file, List<Map<String, Object>> files, Set<String> allowedPluginIds) {
        String fileName = file.getName();

        // Check if it's a system log file (only admin can access)
        if ("sys.log".equals(fileName) || "sys-error.log".equals(fileName) || "visualtool-main.out".equals(fileName)) {
            // Admin already handled above
            return;
        }

        // Check if it's a plugin log file (starts with "plugins")
        if (fileName.startsWith("plugins")) {
            String pluginId = extractPluginIdFromFileName(fileName);

            // Handle plugin ID with underscores (replace underscores with hyphens)
            if (pluginId.contains("_")) {
                pluginId = pluginId.replace("_", "-");
            }

            // Check if user has permission for this plugin
            if (allowedPluginIds.contains(pluginId)) {
                addLogFileInfo(files, file);
            }
        } else {
            // Other non-plugin log files are always allowed
            addLogFileInfo(files, file);
        }
    }

    /**
     * Extract plugin ID from log file name
     *
     * @param fileName log file name
     * @return plugin ID
     */
    private String extractPluginIdFromFileName(String fileName) {
        if (fileName.length() > 8) { // "plugins_" is 8 characters
            // Find the first dot or hyphen after "plugins_"
            int dotIndex = fileName.indexOf(".", 8);
            int dashIndex = fileName.indexOf("-", 8);
            int endIndex = -1;

            if (dotIndex != -1 && dashIndex != -1) {
                endIndex = Math.min(dotIndex, dashIndex);
            } else if (dotIndex != -1) {
                endIndex = dotIndex;
            } else if (dashIndex != -1) {
                endIndex = dashIndex;
            } else {
                endIndex = -1;
            }

            if (endIndex != -1) {
                return fileName.substring(8, endIndex);
            } else {
                // If no dot or dash, take the rest as pluginId
                return fileName.substring(8);
            }
        }
        return "";
    }

    /**
     * List all log files without permission check (for admin user)
     *
     * @return FileList
     */
    private List<Map<String, Object>> listAllLogFilesWithoutPermissionCheck() {
        File dir = new File(loggingFilePath);
        File[] childrenFiles = dir.listFiles();
        List<Map<String, Object>> files = new ArrayList<>();
        if (childrenFiles != null) {
            for (File childFile : childrenFiles) {
                if (childFile.isFile()) {
                    addLogFileInfo(files, childFile);
                }
            }
        }
        return files;
    }

    /**
     * Add log file info to the list
     *
     * @param files     the file list
     * @param childFile the log file
     */
    private void addLogFileInfo(List<Map<String, Object>> files, File childFile) {
        Map<String, Object> file = new HashMap<>();
        file.put("name", childFile.getName());
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
