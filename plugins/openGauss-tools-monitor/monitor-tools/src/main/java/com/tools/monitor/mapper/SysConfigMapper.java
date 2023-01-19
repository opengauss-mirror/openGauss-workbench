package com.tools.monitor.mapper;

import ch.ethz.ssh2.Connection;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tools.monitor.common.contant.Constants;
import com.tools.monitor.config.FileConfig;
import com.tools.monitor.config.PostgresqlConfig;
import com.tools.monitor.config.ZabbixConfig;
import com.tools.monitor.entity.JsonConfig;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.service.SnowFlake;
import com.tools.monitor.service.impl.NagiosServiceImpl;
import com.tools.monitor.util.Base64;
import com.tools.monitor.util.ConnectionUtil;
import com.tools.monitor.util.JsonUtilData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * SysConfigMapper
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
@Slf4j
public class SysConfigMapper {

    @Value("${date.pattern}")
    private String dataPattern;

    @Autowired
    private NagiosServiceImpl nagiosService;

    @Autowired
    private SysSourceTargetMapper sourceTargetMapper;

    private static Connection conn;

    private SnowFlake snowFlake = new SnowFlake(11, 11);

    /**
     * getAllConfig
     *
     * @return list
     */
    public List<SysConfig> getAllConfig() {
        JsonConfig jsonConfig = JsonUtilData.jsonFileToObject(FileConfig.dataSourceConfig, JsonConfig.class);
        if (jsonConfig == null || CollectionUtil.isEmpty(jsonConfig.getSysConfigs())) {
            return new ArrayList<>();
        }
        return jsonConfig.getSysConfigs();
    }

    /**
     * getConfigByid
     *
     * @param id
     * @return SysConfig
     */
    public SysConfig getConfigByid(Long id) {
        List<SysConfig> sysConfigList = getAllConfig();
        SysConfig config = null;
        if (CollectionUtil.isNotEmpty(sysConfigList)) {
            config = sysConfigList.stream().filter(s -> Objects.equals(s.getDataSourceId(), id)).findFirst().orElse(null);
        }
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
            zabbix = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.ZABBIX)).collect(Collectors.toList());
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
        List<SysConfig> nagios = new ArrayList<>();
        SysConfig config = null;
        if (CollectionUtil.isNotEmpty(sysConfigList)) {
            nagios = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.NAGIOS)).collect(Collectors.toList());
        }
        if (CollectionUtil.isNotEmpty(nagios)) {
            config = nagios.get(0);
            config.setClientPassword(Base64.decode(config.getClientPassword()));
            config.setServerPassword(Base64.decode(config.getServerPassword()));
        }
        return config;
    }

    /**
     * getBatchById
     *
     * @param ids
     * @return list
     */
    public List<SysConfig> getBatchById(List<Long> ids) {
        List<SysConfig> sysConfigList = getAllConfig();
        List<SysConfig> result = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                for (SysConfig sysConfig : sysConfigList) {
                    if (id.equals(sysConfig.getDataSourceId())) {
                        result.add(sysConfig);
                    }
                }
            }
        }
        return result;
    }

    /**
     * saveConfig
     *
     * @param sysConfig
     * @return String
     */
    public String saveConfig(SysConfig sysConfig) {
        List<SysConfig> sysConfigList = getAllConfig();
        if (Constants.NAGIOS.equals(sysConfig.getPlatform())) {
            Boolean isServer = getConnection(sysConfig.getServerIp(), sysConfig.getServerName(), sysConfig.getServerPassword());
            if (!isServer) {
                return "Server level connection failed!";
            } else if (StrUtil.isNotBlank(checkServerPath(sysConfig))) {
                return "Incorrect server level path";
            }
            Boolean isClient = getConnection(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
            if (!isClient) {
                return "Client side connection failed!";
            } else if (StrUtil.isNotBlank(checkClientPath(sysConfig))) {
                return "The client side path is incorrect";
            }
            //校验路径
            sysConfig.setClientPassword(Base64.encode(sysConfig.getClientPassword()));
            sysConfig.setServerPassword(Base64.encode(sysConfig.getServerPassword()));
            sysConfig.setDataSourceId(snowFlake.nextId());
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
        List<SysConfig> zabbix = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.ZABBIX)).collect(Collectors.toList());
        if (Constants.ZABBIX.equals(sysConfig.getPlatform()) && CollectionUtil.isNotEmpty(zabbix)) {
            sysConfigList = deleteBatchByIds(Arrays.asList(zabbix.get(0).getDataSourceId()));
        }
        List<SysConfig> nagios = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.NAGIOS)).collect(Collectors.toList());
        if (Constants.NAGIOS.equals(sysConfig.getPlatform()) && CollectionUtil.isNotEmpty(nagios)) {
            sysConfigList = deleteBatchByIds(Arrays.asList(nagios.get(0).getDataSourceId()));
        }
        sysConfigList.add(sysConfig);
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setSysConfigs(sysConfigList);
        JsonUtilData.objectToJsonFile(FileConfig.dataSourceConfig, jsonConfig);
        return "";
    }

    /**
     * checkClientPath
     *
     * @param sysConfig
     * @return String
     */
    private String checkClientPath(SysConfig sysConfig) {
        Connection connection = getConn(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
        String comd = "cat " + sysConfig.getClientPath() + "/etc" + "/nrpe.cfg";
        System.out.println(comd);
        if (StrUtil.isEmpty(nagiosService.executeCmdAndGetResult(comd, connection))) {
            return "The client side path is incorrect";
        }
        return "";
    }

    /**
     * checkServerPath
     *
     * @param sysConfig
     * @return String
     */
    private String checkServerPath(SysConfig sysConfig) {
        Connection connection = getConn(sysConfig.getServerIp(), sysConfig.getServerName(), sysConfig.getServerPassword());
        String comd = "cat " + sysConfig.getServerPath() + "/etc" + "/nagios.cfg";
        if (StrUtil.isEmpty(nagiosService.executeCmdAndGetResult(comd, connection))) {
            return "Incorrect server level path";
        }
        return "";
    }

    /**
     * getConnection
     *
     * @param hostIp hostIp
     * @param userName userName
     * @param password password
     * @return Boolean
     */
    public Boolean getConnection(String hostIp, String userName, String password) {
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Boolean flag = false;
                try {
                    conn = new Connection(hostIp);
                    conn.connect();
                    flag = conn.authenticateWithPassword(userName, password);
                } catch (IOException e) {
                    log.error("ssh_getConnection_{}", e.getMessage());
                } finally {
                    conn.close();
                }
                return flag.toString();
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executorService.submit(task);
            String obj = future.get(1500, TimeUnit.MILLISECONDS);
            if (obj.equals(false)) {
                return false;
            }
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            return false;
        } finally {
            executorService.shutdown();
        }
        return true;
    }

    /**
     * getConn
     *
     * @param hostIp
     * @param userName
     * @param password
     * @return Connection
     */
    public Connection getConn(String hostIp, String userName, String password) {
        boolean flag = false;
        try {
            Connection connection = new Connection(hostIp);
            connection.connect();
            flag = connection.authenticateWithPassword(userName, password);
            if (flag) {
                log.info("Certification successful!");
                return connection;
            } else {
                log.error(hostIp + "Certification fail!");
                connection.close();
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * updateProm
     *
     * @param sysConfig
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
        if (!sysConfig1.getConnectName().equals(sysConfig.getConnectName()) && nameList.contains(sysConfig.getConnectName())) {
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
        JsonUtilData.objectToJsonFile(FileConfig.dataSourceConfig, jsonConfig);
        return "";
    }

    private String checkConfig(SysConfig sysConfig, List<SysConfig> sysConfigList) {
        List<String> connectName = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.PROM)).map(SysConfig::getConnectName).collect(Collectors.toList());
        List<String> ips = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.PROM)).map(SysConfig::getIp).collect(Collectors.toList());
        if (sysConfig.getPlatform().equals(Constants.ZABBIX)) {
            return "";
        }
        String name = connectName.stream().filter(itme -> itme.equals(sysConfig.getConnectName())).findFirst().orElse(null);
        String ip = ips.stream().filter(itme -> itme.equals(sysConfig.getIp())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(name)) {
            return "Instance names cannot be repeated";
        }
        if (ObjectUtil.isNotEmpty(ip)) {
            return "Instance ip cannot be repeated";
        }
        return "";
    }

    private void dealSysConfig(SysConfig sysConfig) {
        if (Constants.PROM.equals(sysConfig.getPlatform())) {
            String url = PostgresqlConfig.getPrefix() + sysConfig.getIp() + StrUtil.C_COLON + sysConfig.getPort() + PostgresqlConfig.getSuffix();
            sysConfig.setUrl(url);
            sysConfig.setDriver(PostgresqlConfig.getDriver());
        } else if (Constants.ZABBIX.equals(sysConfig.getPlatform())) {
            String url = ZabbixConfig.getPrefix() + sysConfig.getIp() + StrUtil.C_COLON + sysConfig.getPort() + StrUtil.SLASH + sysConfig.getDataBaseName() + ZabbixConfig.getSuffix();
            sysConfig.setUrl(url);
            sysConfig.setDriver(ZabbixConfig.getDriver());
        }
        if (sysConfig.getIsCreate()) {
            sysConfig.setDataSourceId(snowFlake.nextId());
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dataPattern);
        LocalDateTime ldt = LocalDateTime.now();
        sysConfig.setCreateTime(ldt.format(dtf));
        sysConfig.setTime(System.currentTimeMillis());
    }

    /**
     * deleteBatchByIds
     *
     * @param ids
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
        Boolean flag = sysConfigs.removeAll(result);
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setSysConfigs(sysConfigs);
        JsonUtilData.objectToJsonFile(FileConfig.dataSourceConfig, jsonConfig);
        return sysConfigs;
    }
}
