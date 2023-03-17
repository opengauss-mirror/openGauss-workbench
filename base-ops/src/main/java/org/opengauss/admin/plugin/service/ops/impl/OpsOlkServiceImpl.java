package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.exception.ops.UserAlreadyExistsException;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.common.utils.uuid.IdUtils;
import org.opengauss.admin.common.utils.uuid.UUID;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.olk.*;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.*;
import org.opengauss.admin.plugin.mapper.ops.OpsOlkMapper;
import org.opengauss.admin.plugin.service.ops.IOpsOlkService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
public class OpsOlkServiceImpl extends ServiceImpl<OpsOlkMapper, OpsOlkEntity> implements IOpsOlkService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private IOpsPackageManagerService packageManagerService;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void install(InstallOlkBody installBody) {
        // create ws
        InstallOlkContext installContext = installBody.getInstallContext();
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        installContext.setRetSession(wsSession);
        installContext.checkConfig();
        // create Task
        InstallOlkContext clone = ObjectUtil.clone(installContext);
        // RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            // RequestContextHolder.setRequestAttributes(context);
            doInstall(clone);
        });
        TaskManager.registry(installBody.getBusinessId(), future);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(String id) {
        removeById(id);
    }

    @Override
    public void destroy(String id) {

    }

    @Override
    public String generateRuleYaml(OlkConfig config) {
        JSONObject databases = new JSONObject(new LinkedHashMap<>());
        AtomicInteger i = new AtomicInteger();
        ShardingRuleDto ruleDto = buildRuleDto(config);
        ShardingDataSourceDto sourceDto = ruleDto.getDataSourceDto();
        String[] users = sourceDto.getUsername().split(",");
        String[] passwords = sourceDto.getPassword().split(",");
        Arrays.stream(sourceDto.getUrl().split(","))
                .map(String::trim)
                .forEach(e -> {
                    databases.fluentPut("ds_" + i.get(), new JSONObject()
                            .fluentPut("url", e)
                            .fluentPut("username", users[i.get()].trim())
                            .fluentPut("password", passwords[i.get()].trim())
                            .fluentPut("connectionTimeoutMilliseconds", 30000)
                            .fluentPut("idleTimeoutMilliseconds", 60000)
                            .fluentPut("maxLifetimeMilliseconds", 1800000)
                            .fluentPut("maxPoolSize", 50)
                            .fluentPut("minPoolSize", 1));
                    i.getAndIncrement();
                });
        JSONObject table = new JSONObject();
        JSONObject sharding = new JSONObject();
        String[] tables = ruleDto.getTableName().split(",");
        String[] columns = ruleDto.getColumn().split(",");
        AtomicInteger k = new AtomicInteger();
        Arrays.stream(tables)
                .map(String::trim)
                .forEach(e -> {
                    String ds = "ds_" + e + "_alg";
                    String ts = "ts_" + e + "_alg";
                    String exp = "ds_${0.." + i.get() + "}." + e + "_${0..2}";
                    String dsExp = "ds_${" + columns[k.get()].trim() + " % " + i.get() + "}";
                    String tsExp = e + "_${" + columns[k.get()].trim() + " % 2}";
                    sharding.fluentPut(ds, new JSONObject()
                                    .fluentPut("type", "INLINE")
                                    .fluentPut("props", new JSONObject()
                                            .fluentPut("algorithm-expression", dsExp)))
                            .fluentPut(ts, new JSONObject()
                                    .fluentPut("type", "INLINE")
                                    .fluentPut("props", new JSONObject()
                                            .fluentPut("algorithm-expression", tsExp)));
                    table.fluentPut(e, new JSONObject()
                            .fluentPut("actualDataNodes", exp)
                            .fluentPut("tableStrategy", new JSONObject()
                                    .fluentPut("standard", new JSONObject()
                                            .fluentPut("shardingColumn", columns[k.get()].trim())
                                            .fluentPut("shardingAlgorithmName", ts)))
                            .fluentPut("databaseStrategy", new JSONObject()
                                    .fluentPut("standard", new JSONObject()
                                            .fluentPut("shardingColumn", columns[k.get()].trim())
                                            .fluentPut("shardingAlgorithmName", ds))));
                    k.getAndIncrement();
                });
        val ret = new JSONObject()
                .fluentPut("databaseName", "sharding_db")
                .fluentPut("dataSources", new JSONObject(new LinkedHashMap<>()).fluentPutAll(databases))
                .fluentPut("rules", new JSONArray()
                        .fluentAdd(new JSONObject()
                                .fluentPut("\\!SHARDING", new JSONObject()
                                        .fluentPut("shardingAlgorithms", new JSONObject().fluentPutAll(sharding))
                                        .fluentPut("tables", new JSONObject().fluentPutAll(table))
                                        .fluentPut("defaultTableStrategy", new JSONObject()
                                                .fluentPut("none", null))
                                        .fluentPut("defaultDatabaseStrategy", new JSONObject()
                                                .fluentPut("none", null)))));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(ret);
    }

    @Override
    public void start(String id) {

    }

    @Override
    public void stop(String id) {

    }

    private void checkInstallConfig(InstallOlkContext installContext) {
        installContext.checkConfig();
    }

    private void doInstall(InstallOlkContext context) {
        WsSession session = context.getRetSession();
        try {
            checkInstallConfig(context);
            wsUtil.sendText(session, OlkProcessFlagStr.START_PROCESS);
            createId(context.getOlkConfig());
            // transfer package to all host
            uploadPackageToHost(context);
            // start distribute deploy jar(install and start)
            startDadService(context.getOlkConfig(), session);
            requestDadInstallService(context.getOlkConfig(), session);
            wsUtil.sendText(session, OlkProcessFlagStr.END_PROCESS);
            wsUtil.setSuccessFlag(context.getRetSession());
        } catch (Exception ex) {
            String errMsg = "Install OpenLooKeng failed, error: " + ex.getMessage();
            log.error(errMsg);
            wsUtil.sendText(session, errMsg);
            wsUtil.setErrorFlag(context.getRetSession());
        }
    }

    private void uploadPackageToHost(InstallOlkContext context) {
        wsUtil.sendText(context.getRetSession(), OlkProcessFlagStr.START_UPLOAD);
        OlkConfig olkConfig = context.getOlkConfig();
        // upload dad
        uploadDad(olkConfig, context.getRetSession());
        // upload sharding and zk
        uploadShardAndZk(olkConfig, context.getRetSession());
        // upload olk
        // uploadOlk(olkConfig, context.getRetSession());
        wsUtil.sendText(context.getRetSession(), OlkProcessFlagStr.END_UPLOAD);
    }

    private void uploadShardAndZk(OlkConfig olkConfig, WsSession wsSession) {
        OpsHostEntity opsHostEntity = hostFacade.getById(olkConfig.getSsInstallHostId());
        if (ObjectUtil.isNull(opsHostEntity)) {
            processError(wsSession, "Can't find host info, please try again");
        }
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), olkConfig.getSsInstallUsername(), encryptionUtils.decrypt(olkConfig.getSsInstallPassword())).orElseThrow(() -> new OpsException("Failed to establish a connection with the target host [ " + opsHostEntity.getPublicIp() + " ]"));
        OpsPackageManagerEntity ssEntity = packageManagerService.getById(olkConfig.getSsTarId());
        if (ObjectUtil.isNull(ssEntity)) {
            processError(wsSession, "Can't find ShardingProxy package, please try again");
        }
        OpsPackageManagerEntity zkEntity = packageManagerService.getById(olkConfig.getZkTarId());
        if (ObjectUtil.isNull(zkEntity)) {
            processError(wsSession, "Can't find Zookeeper package, please try again");
        }
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_SHARDING_PROXY, opsHostEntity.getPublicIp(), olkConfig.getSsInstallUsername()));
        try {
            jschUtil.executeCommand("mkdir -p " + olkConfig.getSsUploadPath(), session);
        } catch (Exception ex) {
            processError(wsSession, String.format("Can't create upload path %s, error: %s", olkConfig.getSsUploadPath(), ex.getMessage()));
        }
        jschUtil.upload(session, wsSession, ssEntity.getRealPath(), getPath(olkConfig.getSsUploadPath(), ssEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_SHARDING_PROXY, opsHostEntity.getPublicIp()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_ZOOKEEPER, opsHostEntity.getPublicIp(), olkConfig.getSsInstallUsername()));
        jschUtil.upload(session, wsSession, zkEntity.getRealPath(), getPath(olkConfig.getSsUploadPath(), zkEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_ZOOKEEPER, opsHostEntity.getPublicIp()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_RULE_YAML, opsHostEntity.getPublicIp()));
        jschUtil.upload(session, wsSession, new ByteArrayInputStream(olkConfig.getRuleYaml().getBytes()), getPath(olkConfig.getSsUploadPath(), "rule.yaml"));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_RULE_YAML, opsHostEntity.getPublicIp()));
        session.disconnect();
    }

    private void uploadOlk(OlkConfig olkConfig, WsSession wsSession) {
        OpsHostEntity opsHostEntity = hostFacade.getById(olkConfig.getOlkInstallHostId());
        if (ObjectUtil.isNull(opsHostEntity)) {
            processError(wsSession, "Can't find host info, please try again");
        }
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), olkConfig.getOlkInstallUsername(), encryptionUtils.decrypt(olkConfig.getOlkInstallPassword())).orElseThrow(() -> new OpsException("Failed to establish a connection with the target host [ " + opsHostEntity.getPublicIp() + " ]"));
        OpsPackageManagerEntity olkEntity = packageManagerService.getById(olkConfig.getOlkTarId());
        if (ObjectUtil.isNull(olkEntity)) {
            processError(wsSession, "Can't find openLooKeng package, please try again");
        }
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_OLK, opsHostEntity.getPublicIp(), olkConfig.getOlkInstallUsername()));
        try {
            jschUtil.executeCommand("mkdir -p " + olkConfig.getOlkUploadPath(), session);
        } catch (Exception ex) {
            processError(wsSession, String.format("Can't create upload path %s, error: %s", olkConfig.getOlkUploadPath(), ex.getMessage()));
        }
        jschUtil.upload(session, wsSession, olkEntity.getRealPath(), getPath(olkConfig.getOlkUploadPath(), olkEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_OLK, opsHostEntity.getPublicIp()));
        session.disconnect();
    }

    private void uploadDad(OlkConfig olkConfig, WsSession wsSession) {
        OpsHostEntity opsHostEntity = hostFacade.getById(olkConfig.getDadInstallHostId());
        if (ObjectUtil.isNull(opsHostEntity)) {
            processError(wsSession, "Can't find host info, please try again");
        }
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), olkConfig.getDadInstallUsername(), encryptionUtils.decrypt(olkConfig.getDadInstallPassword())).orElseThrow(() -> new OpsException("Failed to establish a connection with the target host [ " + opsHostEntity.getPublicIp() + " ]"));
        OpsPackageManagerEntity dadEntity = packageManagerService.getById(olkConfig.getDadTarId());
        if (ObjectUtil.isNull(dadEntity)) {
            processError(wsSession, "Can't find Deployment package, please try again");
        }
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_DAD, opsHostEntity.getPublicIp(), olkConfig.getDadInstallUsername()));
        try {
            jschUtil.executeCommand("mkdir -p " + olkConfig.getDadInstallPath(), session);
        } catch (Exception ex) {
            processError(wsSession, String.format("Can't create upload path %s, error: %s", olkConfig.getDadInstallPath(), ex.getMessage()));
        }

        jschUtil.upload(session, wsSession, dadEntity.getRealPath(), getPath(olkConfig.getDadInstallPath(), dadEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_DAD, opsHostEntity.getPublicIp()));
        session.disconnect();
    }

    private void startDadService(OlkConfig config, WsSession wsSession) {
        OpsPackageManagerEntity entity = packageManagerService.getById(config.getDadTarId());
        if (ObjectUtil.isNull(entity)) {
            processError(wsSession, "Can't find deployment package, please try again");
        }
        OpsHostEntity host = hostFacade.getById(config.getDadInstallHostId());
        if (ObjectUtil.isNull(host)) {
            processError(wsSession, "Can't find deployment host info, please try again");
        }
        // check if already started
        ServerDto serverDto = new ServerDto();
        serverDto.setIp(host.getPublicIp());
        serverDto.setPort(host.getPort());
        serverDto.setUsername(config.getDadInstallUsername());
        serverDto.setPassword(encryptionUtils.decrypt(config.getDadInstallPassword()));
        boolean isRunning = isProcessRunning(config.getDadPort(), OlkProcessFlagStr.DAD_PKG_NAME, serverDto, wsSession);
        if (isRunning) {
            wsUtil.sendText(wsSession, "Deployment service is alreay running, skip start");
            return;
        }
        Session session = jschUtil.getSession(host.getPublicIp(), host.getPort(), config.getDadInstallUsername(), encryptionUtils.decrypt(config.getDadInstallPassword())).orElseThrow(() -> new OpsException("Connection establishment failed"));
        try {
            String cmd = String.format("cd %s; nohup java \'-Dserver.port=%s\' -Dpath=/opt -jar %s > %s 2>&1 &", config.getDadInstallPath(), config.getDadPort(), entity.getFileName(), getPath(config.getDadInstallPath(), DadReqPath.OUTPUT_LOG));
            JschResult result = jschUtil.executeCommand(cmd, session, wsSession);
            if (result.getExitCode() != 0) {
                processError(wsSession, "Use nohup to start deployment jar failed, exitCode: " + result.getExitCode());
            }
            // Judge whether it has been started
            int maxTryCount = 3;
            int tryCount = 0;
            boolean flag = false;
            while (tryCount < maxTryCount) {
                isRunning = isProcessRunning(config.getDadPort(), OlkProcessFlagStr.DAD_PKG_NAME, serverDto, wsSession);
                if (isRunning) {
                    flag = true;
                    break;
                } else {
                    tryCount++;
                    Thread.sleep(5000);
                }
            }
            if (!flag) {
                throw new OpsException("Failed to start deployment service after 15s, please try again");
            }
        } catch (Exception ex) {
            processError(wsSession, "Failed to start deployment service, error: " + ex.getMessage());
        } finally {
            session.disconnect();
        }
    }

    private void requestDadInstallService(OlkConfig config, WsSession wsSession) {
        wsUtil.sendText(wsSession, OlkProcessFlagStr.RUN_DAD_SERVICE);
        OpsHostEntity host = hostFacade.getById(config.getDadInstallHostId());
        if (ObjectUtil.isNull(host)) {
            processError(wsSession, "Can't find deployment host info, please try again");
        }

        ServerDto serverDto = new ServerDto();
        serverDto.setIp(host.getPublicIp());
        serverDto.setPort(host.getPort());
        serverDto.setUsername(config.getDadInstallUsername());
        serverDto.setPassword(encryptionUtils.decrypt(config.getDadInstallPassword()));

        OlkCommandDto olkCommandDto = new OlkCommandDto();
        ShardingsDto ssDto = buildShardDto(config, wsSession);
        OpenLooKengDto olkDto = buildOlkDto(config, wsSession);
        olkCommandDto.setShardingsDto(ssDto);
        olkCommandDto.setOpenLooKengDto(olkDto);

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<OlkCommandDto> entity = new HttpEntity<>(olkCommandDto, headers);
        String reqUrl = String.format("http://%s:%s/%s", host.getPublicIp(), config.getDadPort(), DadReqPath.DEPLOY);

        String resultStr = HttpUtils.sendPost(reqUrl, JSON.toJSONString(olkCommandDto));
        DadResult result = JSON.parseObject(resultStr, DadResult.class);
        if (result != null && result.isSuccess()) {
            String logPrefix = (String) result.get(DadResult.MSG_TAG);
            readLogToWs(serverDto, config.getDadLogFileName(), logPrefix, wsSession);
            wsUtil.sendText(wsSession, OlkProcessFlagStr.RUN_DAD_SERVICE_COMPLETE);
        } else {
            String logPrefix = (String) result.get(DadResult.DATA_TAG);
            readLogToWs(serverDto, config.getDadLogFileName(), logPrefix, wsSession);
            processError(wsSession, "Deployment service process failed, error: " + result.get(DadResult.MSG_TAG));
        }
    }

    private ShardingsDto buildShardDto(OlkConfig config, WsSession session) {
        ShardingsDto shardingsDto = new ShardingsDto();

        ServerDto shardingServer = new ServerDto();
        OpsHostEntity ssHost = hostFacade.getById(config.getSsInstallHostId());
        if (ObjectUtil.isNull(ssHost)) {
            processError(session, "Can't find host info, please try again");
        }
        shardingServer.setIp(ssHost.getPublicIp());
        shardingServer.setPort(ssHost.getPort());
        shardingServer.setUsername(config.getSsInstallUsername());
        shardingServer.setPassword(encryptionUtils.decrypt(config.getSsInstallPassword()));
        shardingsDto.setServerDto(shardingServer);

        shardingsDto.setPort(Integer.parseInt(config.getSsPort()));
        shardingsDto.setShardingRuleDto(buildRuleDto(config));
        shardingsDto.setSourcePath(config.getSsUploadPath());
        shardingsDto.setInstallPath(config.getSsInstallPath());

        ZookeeperDto zookeeperDto = new ZookeeperDto();
        zookeeperDto.setPort(Integer.parseInt(config.getZkPort()));
        shardingsDto.setZookeeperDto(zookeeperDto);
        return shardingsDto;
    }

    private ShardingRuleDto buildRuleDto(OlkConfig config) {
        ShardingRuleDto ruleDto = new ShardingRuleDto();
        StringBuilder urlBuilder = new StringBuilder();
        StringBuilder usernameBuilder = new StringBuilder();
        StringBuilder pwdBuilder = new StringBuilder();
        for (ShardingDatasourceConfig dsItem : config.getDsConfig()) {
            urlBuilder.append(String.format("jdbc:opengauss://%s:%s/%s", dsItem.getHost(), dsItem.getPort(), dsItem.getDbName())).append(",");
            usernameBuilder.append(dsItem.getUsername()).append(",");
            pwdBuilder.append(dsItem.getPassword()).append(",");
        }
        ShardingDataSourceDto dataSourceDto = new ShardingDataSourceDto();
        dataSourceDto.setUrl(urlBuilder.substring(0, urlBuilder.length() - 1));
        dataSourceDto.setUsername(usernameBuilder.substring(0, usernameBuilder.length() - 1));
        dataSourceDto.setPassword(pwdBuilder.substring(0, pwdBuilder.length() - 1));
        ruleDto.setDataSourceDto(dataSourceDto);
        ruleDto.setColumn(config.getTableName());
        ruleDto.setTableName(config.getColumn());
        return ruleDto;
    }

    private OpenLooKengDto buildOlkDto(OlkConfig config, WsSession session) {
        OpenLooKengDto olkDto = new OpenLooKengDto();

        ServerDto shardingServer = new ServerDto();
        OpsHostEntity olkHost = hostFacade.getById(config.getOlkInstallHostId());
        if (ObjectUtil.isNull(olkHost)) {
            processError(session, "Can't find host info, please try again");
        }
        shardingServer.setIp(olkHost.getPublicIp());
        shardingServer.setPort(olkHost.getPort());
        shardingServer.setUsername(config.getSsInstallUsername());
        shardingServer.setPassword(encryptionUtils.decrypt(config.getSsInstallPassword()));
        olkDto.setServerDto(shardingServer);

        olkDto.setInstallPath(config.getOlkInstallPath());
        olkDto.setSourcePath(config.getOlkUploadPath());
        olkDto.setPort(Integer.parseInt(config.getOlkPort()));
        olkDto.setOlkConfigDto(config.getOlkParamConfig());
        return olkDto;
    }

    /**
     * read log to ws, you must call this method before execute request
     *
     * @param serverDto    host info
     * @param logPath      log file path
     * @param startFlagStr read start flag
     * @param endFlagStr   read end flag
     * @param wsSession    ws session
     */
    private void threadReadLogToWs(ServerDto serverDto, String logPath, String startFlagStr, String endFlagStr, WsSession wsSession, Object lockObject) {
        new Thread(() -> {
            try {
                // Read the log file line by line
                // must wait to link to log file
                Session session = jschUtil.getSession(serverDto.getIp(), serverDto.getPort(), serverDto.getUsername(), encryptionUtils.decrypt(serverDto.getPassword())).orElseThrow(() -> new OpsException("Connection establishment failed"));
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setPtyType("dump");
                channel.setPty(false);
                channel.setCommand("tail -f " + logPath);
                channel.connect(20000);
                InputStream in = channel.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                synchronized (lockObject) {
                    lockObject.notify();
                }
                String line;
                boolean isStarted = false;
                while ((line = reader.readLine()) != null) {
                    // Check for the start and end flags
                    if (line.contains(startFlagStr)) {
                        // Start sending log messages to the WebSocket
                        wsUtil.sendText(wsSession, line);
                        isStarted = true;
                    }
                    if (isStarted) {
                        wsUtil.sendText(wsSession, line);
                    }
                    if (line.contains(endFlagStr)) {
                        // Start sending log messages to the WebSocket
                        wsUtil.sendText(wsSession, line);
                        break;
                    }
                }
                channel.disconnect();
                session.disconnect();
                reader.close();
            } catch (Exception ex) {
                processError(wsSession, String.format("Failed to Read process log %s, error: %s", logPath, ex.getMessage()));
            }
        }).start();
    }

    private void readLogToWs(ServerDto serverDto, String logPath, String reqId, WsSession wsSession) {
        try {
            // Read the log file line by line
            // must wait to link to log file
            Session session = jschUtil.getSession(serverDto.getIp(), serverDto.getPort(), serverDto.getUsername(), serverDto.getPassword()).orElseThrow(() -> new OpsException("Connection establishment failed"));
            JschResult result = jschUtil.executeCommand(String.format("cat %s | grep %s", logPath, reqId), session);
            if (result.getExitCode() != 0) {
                processError(wsSession, String.format("Failed to Read process log %s, error: %s", logPath, result.getExitCode()));
            }
            wsUtil.sendText(wsSession, result.getResult());
            session.disconnect();
        } catch (Exception ex) {
            processError(wsSession, String.format("Failed to Read process log %s, error: %s", logPath, ex.getMessage()));
        }
    }

    private void processError(WsSession session, String errMsg) {
        wsUtil.sendText(session, errMsg);
        wsUtil.setErrorFlag(session);
        throw new OpsException(errMsg);
    }

    private boolean isProcessRunning(String processPort, String searchKey, ServerDto serverDto, WsSession wsSession) {
        String cmd = String.format("netstat -anp | grep -w %s | awk '{print $7}' | sed 's|/.*||'", processPort);
        Session session = jschUtil.getSession(serverDto.getIp(), serverDto.getPort(), serverDto.getUsername(), serverDto.getPassword()).orElseThrow(() -> new OpsException(String.format("Connection to %s establishment failed", serverDto.getIp())));
        try {
            JschResult result = jschUtil.executeCommand(cmd, session, wsSession);
            if (result.getExitCode() != 0) {
                processError(wsSession, "Find dad process error, exitCode: " + result.getExitCode());
            }
            String pid = result.getResult();
            if (StrUtil.isEmpty(pid) || pid.equals("-")) {
                return false;
            }
            if (pid.contains("\r\n")) {
                pid = pid.split("\r\n")[0];
            }
            result = jschUtil.executeCommand("ps -aux|grep " + pid, session, wsSession);
            if (result.getExitCode() != 0) {
                processError(wsSession, "Find dad process error, exitCode: " + result.getExitCode());
            }
            return result.getResult().contains(searchKey);
        } catch (Exception ex) {
            processError(wsSession, "Find dad process error, error: " + ex.getMessage());
        } finally {
            session.disconnect();
        }
        return false;
    }

    private void createId(OlkConfig olkConfig) {
        olkConfig.setId(IdUtils.fastUuid());
    }

    private String getPath(String path1, String path2) {
        return Path.of(path1, path2).toString().replace("\\", "/");
    }
}
