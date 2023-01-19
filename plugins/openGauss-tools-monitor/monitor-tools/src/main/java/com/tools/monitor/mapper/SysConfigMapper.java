/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.mapper;

import ch.ethz.ssh2.Connection;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tools.monitor.common.contant.ConmmonShare;
import com.tools.monitor.config.FileConfig;
import com.tools.monitor.config.PostgresqlConfig;
import com.tools.monitor.config.ZabbixConfig;
import com.tools.monitor.entity.JsonConfig;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.service.MonitorFlake;
import com.tools.monitor.service.impl.NagiosServiceImpl;
import com.tools.monitor.util.Base64;
import com.tools.monitor.util.ConnectionUtil;
import com.tools.monitor.util.JsonUtilData;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SysConfigMapper
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
@Slf4j
public class SysConfigMapper {
    private static Connection conn;

    private static final Connection EMPY = null;

    private static final String LINE_FEED = String.valueOf(StrUtil.C_SLASH);

    private MonitorFlake MonitorFlake = new MonitorFlake(11, 11);

    @Value("${date.pattern}")
    private String dataPattern;

    @Autowired
    private NagiosServiceImpl nagiosService;

    @Autowired
    private SysSourceTargetMapper sourceTargetMapper;

    /**
     * getAllConfig
     *
     * @return list
     */
    public List<SysConfig> getAllConfig() {
        JsonConfig jsonConfig = JsonUtilData.jsonFileToObject(FileConfig.getDataSourceConfig(), JsonConfig.class);
        if (jsonConfig == null || CollectionUtil.isEmpty(jsonConfig.getSysConfigs())) {
            return new ArrayList<>();
        }
        return jsonConfig.getSysConfigs();
    }

    /**
     * getConfigByid
     *
     * @param id id
     * @return SysConfig
     */
    public SysConfig getConfigByid(Long id) {
        List<SysConfig> sysConfigList = getAllConfig();
        SysConfig config = null;
        config = sysConfigList.stream()
                .filter(s -> Objects.equals(s.getDataSourceId(), id))
                .findFirst().orElse(null);
        return config;
    }

    /**
     * getZabbixConfig
     *
     * @return SysConfig
     */
    public SysConfig getZabbixConfig() {
        List<SysConfig> sysConfigList = getAllConfig();
        List<SysConfig> zabbix = new ArrayList<>();
        SysConfig config = null;
        if (CollectionUtil.isNotEmpty(sysConfigList)) {
            zabbix = sysConfigList.stream()
                    .filter(item -> item.getPlatform().equals(ConmmonShare.ZABBIX))
                    .collect(Collectors.toList());
        }
        if (CollectionUtil.isNotEmpty(zabbix)) {
            config = zabbix.get(0);
            config.setPassword(Base64.decode(config.getPassword()));
        }
        return config;
    }

    /**
     * getNagiosConfig
     *
     * @return SysConfig
     */
    public SysConfig getNagiosConfig() {
        List<SysConfig> sysConfigList = getAllConfig();
        SysConfig config = null;
        if (CollectionUtil.isNotEmpty(sysConfigList)) {
            config = sysConfigList.stream()
                    .filter(item -> item.getPlatform().equals(ConmmonShare.NAGIOS)).findFirst().orElse(null);
        }
        return config;
    }

    /**
     * getBatchById
     *
     * @param ids ids
     * @return list
     */
    public List<SysConfig> getBatchById(List<Long> ids) {
        List<SysConfig> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(ids)) {
            return result;
        }
        List<SysConfig> sysConfigList = getAllConfig();
        for (Long id : ids) {
            for (SysConfig sysConfig : sysConfigList) {
                if (id.equals(sysConfig.getDataSourceId())) {
                    result.add(sysConfig);
                }
            }
        }
        return result;
    }

    /**
     * saveConfig
     *
     * @param sysConfig sysConfig
     * @return String
     */
    public String saveConfig(SysConfig sysConfig) {
        List<SysConfig> sysConfigList = getAllConfig();
        if (ConmmonShare.NAGIOS.equals(sysConfig.getPlatform())) {
            Boolean isServer =
                    getConnection(sysConfig.getServerIp(), sysConfig.getServerName(), sysConfig.getServerPassword());
            if (!isServer) {
                return "Server level connection failed!";
            } else if (StrUtil.isNotBlank(checkServerPath(sysConfig))) {
                return "Incorrect server level path";
            } else {
                log.info("saveConfig");
            }
            Boolean isClient =
                    getConnection(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
            if (!isClient) {
                return "Client side connection failed!";
            } else if (StrUtil.isNotBlank(checkClientPath(sysConfig))) {
                return "The client side path is incorrect";
            } else {
                log.info("saveConfig");
            }
            sysConfig.setClientPassword(Base64.encode(sysConfig.getClientPassword()));
            sysConfig.setServerPassword(Base64.encode(sysConfig.getServerPassword()));
            sysConfig.setDataSourceId(MonitorFlake.nextId());
        } else {
            dealSysConfig(sysConfig);
            String message = checkConfig(sysConfig, sysConfigList);
            if (StrUtil.isNotBlank(message)) {
                return message;
            }
            String msg = ConnectionUtil.getConnection(sysConfig);
            if (StrUtil.isNotBlank(msg)) {
                return msg;
            }
            sysConfig.setPassword(Base64.encode(sysConfig.getPassword()));
        }
        List<SysConfig> zabbix = sysConfigList.stream()
                .filter(item -> item.getPlatform().equals(ConmmonShare.ZABBIX)).collect(Collectors.toList());
        if (ConmmonShare.ZABBIX.equals(sysConfig.getPlatform()) && CollectionUtil.isNotEmpty(zabbix)) {
            sysConfigList = deleteBatchByIds(Arrays.asList(zabbix.get(0).getDataSourceId()));
        }
        List<SysConfig> nagios = sysConfigList.stream()
                .filter(item -> item.getPlatform().equals(ConmmonShare.NAGIOS)).collect(Collectors.toList());
        if (ConmmonShare.NAGIOS.equals(sysConfig.getPlatform()) && CollectionUtil.isNotEmpty(nagios)) {
            sysConfigList = deleteBatchByIds(Arrays.asList(nagios.get(0).getDataSourceId()));
        }
        sysConfigList.add(sysConfig);
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setSysConfigs(sysConfigList);
        JsonUtilData.objectToJsonFile(FileConfig.getDataSourceConfig(), jsonConfig);
        return "";
    }

    private String checkClientPath(SysConfig sysConfig) {
        Connection connection =
                getConn(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
        String comd = "cat " + sysConfig.getClientPath() + LINE_FEED + "etc" + LINE_FEED + "nrpe.cfg";
        if (StrUtil.isEmpty(nagiosService.executeCmdAndGetResult(comd, connection))) {
            return "The client side path is incorrect";
        }
        return "";
    }

    private String checkServerPath(SysConfig sysConfig) {
        Connection connection =
                getConn(sysConfig.getServerIp(), sysConfig.getServerName(), sysConfig.getServerPassword());
        String comd = "cat " + sysConfig.getServerPath() + LINE_FEED + "etc" + LINE_FEED + "nagios.cfg";
        if (StrUtil.isEmpty(nagiosService.executeCmdAndGetResult(comd, connection))) {
            return "Incorrect server level path";
        }
        return "";
    }

    /**
     * getConnection
     *
     * @param hostIp   hostIp
     * @param userName userName
     * @param password password
     * @return Boolean
     */
    public Boolean getConnection(String hostIp, String userName, String password) {
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Boolean isFlag = false;
                try {
                    conn = new Connection(hostIp);
                    conn.connect();
                    isFlag = conn.authenticateWithPassword(userName, password);
                } catch (IOException e) {
                    log.error("ssh_getConnection_{}", e.getMessage());
                } finally {
                    conn.close();
                }
                return isFlag.toString();
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        try {
            Future<String> future = threadPoolExecutor.submit(task);
            String obj = future.get(1500, TimeUnit.MILLISECONDS);
            if (obj.equals(false)) {
                return false;
            }
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            return false;
        } finally {
            threadPoolExecutor.shutdown();
        }
        return true;
    }

    /**
     * getConn
     *
     * @param hostIp   hostIp
     * @param userName userName
     * @param password password
     * @return Connection
     */
    public Connection getConn(String hostIp, String userName, String password) {
        boolean isFlag = false;
        try {
            Connection connection = new Connection(hostIp);
            connection.connect();
            isFlag = connection.authenticateWithPassword(userName, password);
            if (isFlag) {
                log.info("auth success!");
                return connection;
            } else {
                log.error(hostIp + "auth fail!");
                connection.close();
                return EMPY;
            }
        } catch (IOException exception) {
            return EMPY;
        }
    }

    /**
     * updateProm
     *
     * @param sysConfig sysConfig
     * @return String
     */
    public String updateProm(SysConfig sysConfig) {
        List<SysConfig> sysConfigList = getAllConfig();
        List<String> nameList = sysConfigList.stream().map(SysConfig::getConnectName).collect(Collectors.toList());
        dealSysConfig(sysConfig);
        SysConfig sysConfig1 = getConfigByid(sysConfig.getDataSourceId());
        SysSourceTarget sysSourceTarget = sourceTargetMapper.sysSourceTargetById(sysConfig.getDataSourceId());
        if (!sysConfig.getIp().equals(sysConfig1.getIp())) {
            return "Modification of IP is not allowed.";
        }
        if (!sysConfig1.getConnectName().equals(sysConfig.getConnectName())
                && nameList.contains(sysConfig.getConnectName())) {
            return "Instance names cannot be repeated";
        }
        String msg = ConnectionUtil.getConnection(sysConfig);
        if (StrUtil.isNotBlank(msg)) {
            return msg;
        }
        sysConfig.setPassword(Base64.encode(sysConfig.getPassword()));
        List<SysConfig> newList = deleteBatchByIds(Arrays.asList(sysConfig.getDataSourceId()));
        newList.add(sysConfig);
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setSysConfigs(newList);
        JsonUtilData.objectToJsonFile(FileConfig.getDataSourceConfig(), jsonConfig);
        return "";
    }

    private String checkConfig(SysConfig sysConfig, List<SysConfig> sysConfigList) {
        if (sysConfig.getPlatform().equals(ConmmonShare.ZABBIX)) {
            return "";
        }
        String name = null;
        String ip = null;
        if (CollectionUtil.isEmpty(sysConfigList)) {
            return "";
        }
        for (SysConfig config : sysConfigList) {
            if (config.getPlatform().equals(ConmmonShare.PROM)) {
                if (config.getConnectName().equals(sysConfig.getConnectName())) {
                    name = config.getConnectName();
                }
                if (config.getIp().equals(sysConfig.getIp())) {
                    ip = config.getIp();
                }
            }
        }
        if (ObjectUtil.isNotEmpty(name)) {
            return "Instance names cannot be repeated";
        }
        if (ObjectUtil.isNotEmpty(ip)) {
            return "Instance ip cannot be repeated";
        }
        return "";
    }

    private void dealSysConfig(SysConfig sysConfig) {
        if (ConmmonShare.PROM.equals(sysConfig.getPlatform())) {
            String url = PostgresqlConfig.getPrefix() + sysConfig.getIp()
                    + StrUtil.C_COLON + sysConfig.getPort() + PostgresqlConfig.getSuffix();
            sysConfig.setUrl(url);
            sysConfig.setDriver(PostgresqlConfig.getDriver());
        }
        if (ConmmonShare.ZABBIX.equals(sysConfig.getPlatform())) {
            String url = ZabbixConfig.getPrefix() + sysConfig.getIp() + StrUtil.C_COLON
                    + sysConfig.getPort() + StrUtil.SLASH + sysConfig.getDataBaseName() + ZabbixConfig.getSuffix();
            sysConfig.setUrl(url);
            sysConfig.setDriver(ZabbixConfig.getDriver());
        }
        if (sysConfig.getIsCreate()) {
            sysConfig.setDataSourceId(MonitorFlake.nextId());
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dataPattern);
        LocalDateTime ldt = LocalDateTime.now();
        sysConfig.setCreateTime(ldt.format(dtf));
        sysConfig.setTime(System.currentTimeMillis());
    }

    /**
     * deleteBatchByIds
     *
     * @param ids ids
     * @return list
     */
    public List<SysConfig> deleteBatchByIds(List<Long> ids) {
        List<SysConfig> sysConfigs = getAllConfig();
        List<SysConfig> result = new ArrayList<>();
        for (SysConfig sysConfig : sysConfigs) {
            for (Long id : ids) {
                if (ObjectUtil.isNotEmpty(id) && id.equals(sysConfig.getDataSourceId())) {
                    result.add(sysConfig);
                }
            }
        }
        sysConfigs.removeAll(result);
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setSysConfigs(sysConfigs);
        JsonUtilData.objectToJsonFile(FileConfig.getDataSourceConfig(), jsonConfig);
        return sysConfigs;
    }
}
