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
 * OpsOlkServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsOlkServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.common.utils.uuid.IdUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkContext;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkPageVO;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkProcessFlagStr;
import org.opengauss.admin.plugin.domain.model.ops.olk.ShardingDatasourceConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadReqPath;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadResult;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OlkCommandDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OpenLooKengDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ServerDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingDataSourceDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingRuleDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingsDto;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ZookeeperDto;
import org.opengauss.admin.plugin.mapper.ops.OpsOlkMapper;
import org.opengauss.admin.plugin.service.ops.IOpsOlkService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    @Value("${olk.readLogTimeout}")
    private int readLogTimeout;
    @Value("${olk.startLoopCount}")
    private int startLoopCount;
    @Value("${olk.startLoopWaitTime}")
    private int startLoopWaitTime;

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
    @Transactional(rollbackFor = Exception.class)
    public void destroy(String id, String bid) {
        requestNormalService(id, bid, DadReqPath.DESTROY, OlkProcessFlagStr.START_DESTROY, OlkProcessFlagStr.END_DESTROY, OlkProcessFlagStr.END_DESTROY_IN_ERROR);
        removeById(id);
    }

    private void requestNormalService(String id, String bid, String path, String startFlag, String endFlag, String endErrFlag) {
        OpsOlkEntity olkEntity = getById(id);
        if (olkEntity == null) {
            throw new OpsException("OpenLooKeng deployment info not exists, please refresh and try again");
        }
        WsSession wsSession = wsConnectorManager.getSession(bid).orElseThrow(() -> new OpsException("websocket session not exist"));
        ServerDto serverDto = new ServerDto();
        OpsHostEntity hostEntity = hostFacade.getById(olkEntity.getDadInstallHostId());
        if (hostEntity == null) {
            throw new OpsException("OpenLooKeng distribute deploy host not exists, please refresh and try again");
        }
        serverDto.setIp(hostEntity.getPublicIp());
        serverDto.setPort(hostEntity.getPort());
        serverDto.setUsername(olkEntity.getDadInstallUsername());
        serverDto.setPassword(encryptionUtils.decrypt(olkEntity.getDadInstallPassword()));

        String reqUrl = String.format("http://%s:%s/%s", hostEntity.getPublicIp(), olkEntity.getDadPort(), path);
        HttpUtils.PostResponse response = HttpUtils.sendPost(reqUrl, JSON.toJSONString(serverDto));
        DadResult result = JSON.parseObject(response.getBody(), DadResult.class);
        String reqId = response.getHeader(DadReqPath.REQ_ID_KEY);
        if (StrUtil.isEmpty(reqId)) {
            processError(wsSession, "Send command to deployment service failed, Can't find req id");
        }
        if (result != null && result.isSuccess()) {
            ReadLogParam param = ReadLogParam.builder().serverDto(serverDto).logPath(getDadLogFileName(olkEntity.getDadInstallPath())).startFlag(startFlag).endFlag(endFlag).errFlag(endErrFlag).reqId(response.getHeader(DadReqPath.REQ_ID_KEY)).wsSession(wsSession).build();
            threadReadLogToWs(param);
            wsUtil.sendText(wsSession, OlkProcessFlagStr.SEND_CMD_TO_SERVICE_COMPLETE);
        } else {
            processError(wsSession, "Send command to deployment service failed, error: " + result.get(DadResult.MSG_TAG));
        }
    }

    @Override
    public String generateRuleYaml(OlkConfig config) {
        JSONObject databases = new JSONObject(new LinkedHashMap<>());
        AtomicInteger i = new AtomicInteger();
        ShardingRuleDto ruleDto = buildRuleDto(config);
        ShardingDataSourceDto sourceDto = ruleDto.getDataSourceDto();
        String[] users = sourceDto.getUsername().split(",");
        String[] passwords = sourceDto.getPassword().split(",");
        Arrays.stream(sourceDto.getUrl().split(",")).map(String::trim).forEach(e -> {
            databases.fluentPut("ds_" + i.get(), new JSONObject().fluentPut("url", e).fluentPut("username", users[i.get()].trim()).fluentPut("password", passwords[i.get()].trim()).fluentPut("connectionTimeoutMilliseconds", 30000).fluentPut("idleTimeoutMilliseconds", 60000).fluentPut("maxLifetimeMilliseconds", 1800000).fluentPut("maxPoolSize", 50).fluentPut("minPoolSize", 1));
            i.getAndIncrement();
        });
        JSONObject table = new JSONObject();
        JSONObject sharding = new JSONObject();
        String[] tables = ruleDto.getTableName().split(",");
        String[] columns = ruleDto.getColumn().split(",");
        AtomicInteger k = new AtomicInteger();
        Arrays.stream(tables).map(String::trim).forEach(e -> {
            String ds = "ds_" + e + "_alg";
            String ts = "ts_" + e + "_alg";
            String exp = "ds_${0.." + i.get() + "}." + e + "_${0..2}";
            String dsExp = "ds_${" + columns[k.get()].trim() + " % " + i.get() + "}";
            String tsExp = e + "_${" + columns[k.get()].trim() + " % 2}";
            sharding.fluentPut(ds, new JSONObject().fluentPut("type", "INLINE").fluentPut("props", new JSONObject().fluentPut("algorithm-expression", dsExp))).fluentPut(ts, new JSONObject().fluentPut("type", "INLINE").fluentPut("props", new JSONObject().fluentPut("algorithm-expression", tsExp)));
            table.fluentPut(e, new JSONObject().fluentPut("actualDataNodes", exp).fluentPut("tableStrategy", new JSONObject().fluentPut("standard", new JSONObject().fluentPut("shardingColumn", columns[k.get()].trim()).fluentPut("shardingAlgorithmName", ts))).fluentPut("databaseStrategy", new JSONObject().fluentPut("standard", new JSONObject().fluentPut("shardingColumn", columns[k.get()].trim()).fluentPut("shardingAlgorithmName", ds))));
            k.getAndIncrement();
        });
        val ret = new JSONObject().fluentPut("databaseName", "sharding_db").fluentPut("dataSources", new JSONObject(new LinkedHashMap<>()).fluentPutAll(databases)).fluentPut("rules", new JSONArray().fluentAdd(new JSONObject().fluentPut("\\!SHARDING", new JSONObject().fluentPut("shardingAlgorithms", new JSONObject().fluentPutAll(sharding)).fluentPut("tables", new JSONObject().fluentPutAll(table)).fluentPut("defaultTableStrategy", new JSONObject().fluentPut("none", null)).fluentPut("defaultDatabaseStrategy", new JSONObject().fluentPut("none", null)))));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(ret);
    }

    @Override
    public void start(String id, String bid) {
        requestNormalService(id, bid, DadReqPath.START, OlkProcessFlagStr.START_SERVICE, OlkProcessFlagStr.END_START_SERVICE, OlkProcessFlagStr.END_START_SERVICE_IN_ERROR);
    }

    @Override
    public void stop(String id, String bid) {
        requestNormalService(id, bid, DadReqPath.STOP, OlkProcessFlagStr.START_STOP_SERVICE, OlkProcessFlagStr.END_STOP_SERVICE, OlkProcessFlagStr.END_STOP_SERVICE_IN_ERROR);
    }

    @Override
    public IPage<OlkPageVO> pageOlk(Page pageReq, String name) {
        LambdaQueryWrapper<OpsOlkEntity> queryWrapper = Wrappers.lambdaQuery(OpsOlkEntity.class).like(StrUtil.isNotEmpty(name), OpsOlkEntity::getName, name);
        IPage<OpsOlkEntity> entityPage = page(pageReq, queryWrapper);
        List<OpsHostEntity> hostEntityList = hostFacade.listAll();
        List<OpsPackageManagerEntity> managerEntities = packageManagerService.list();
        List<OlkPageVO> voList = new ArrayList<>();
        for (OpsOlkEntity item : entityPage.getRecords()) {
            OlkPageVO vo = new OlkPageVO();
            BeanUtils.copyProperties(item, vo);
            for (OpsHostEntity hostEntity : hostEntityList) {
                if (hostEntity.getHostId().equals(item.getDadInstallHostId())) {
                    vo.setDadInstallIp(hostEntity.getPublicIp());
                }
                if (hostEntity.getHostId().equals(item.getSsInstallHostId())) {
                    vo.setSsInstallIp(hostEntity.getPublicIp());
                }
                if (hostEntity.getHostId().equals(item.getOlkInstallHostId())) {
                    vo.setOlkInstallIp(hostEntity.getPublicIp());
                }
            }
            for (OpsPackageManagerEntity pkgEntity : managerEntities) {
                if (pkgEntity.getPackageId().equals(item.getDadTarId())) {
                    vo.setDadPkgName(pkgEntity.getFileName());
                }
                if (pkgEntity.getPackageId().equals(item.getSsTarId())) {
                    vo.setSsPkgName(pkgEntity.getFileName());
                }
                if (pkgEntity.getPackageId().equals(item.getZkTarId())) {
                    vo.setZkPkgName(pkgEntity.getFileName());
                }
                if (pkgEntity.getPackageId().equals(item.getOlkTarId())) {
                    vo.setOlkPkgName(pkgEntity.getFileName());
                }
            }
            voList.add(vo);
        }
        IPage<OlkPageVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setPages(entityPage.getPages());
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setTotal(entityPage.getTotal());
        voPage.setSize(entityPage.getSize());
        return voPage;
    }

    private void checkInstallConfig(InstallOlkContext installContext) {
        installContext.checkConfig();
    }

    @Transactional(rollbackFor = Exception.class)
    public void doInstall(InstallOlkContext context) {
        WsSession session = context.getRetSession();
        try {
            checkInstallConfig(context);
            wsUtil.sendText(session, OlkProcessFlagStr.START_DEPLOY_PROCESS);
            createId(context.getOlkConfig());
            // transfer package to all host
            uploadPackageToHost(context);
            // start distribute deploy jar(install and start)
            startDadService(context.getOlkConfig(), session);
            requestDadInstallService(context.getOlkConfig(), session);
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
        uploadOlk(olkConfig, context.getRetSession());
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
            JschResult result = jschUtil.executeCommand("mkdir -p " + olkConfig.getSsUploadPath(), session);
            if (result.getExitCode() != 0) {
                throw new OpsException(result.getResult());
            }
        } catch (Exception ex) {
            processError(wsSession, String.format("Can't create upload path %s, error: %s", olkConfig.getSsUploadPath(), ex.getMessage()));
        }
        jschUtil.upload(session, wsSession, ssEntity.getRealPath(), getPath(olkConfig.getSsUploadPath(), ssEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_SHARDING_PROXY, opsHostEntity.getPublicIp()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_ZOOKEEPER, opsHostEntity.getPublicIp(), olkConfig.getSsInstallUsername()));
        jschUtil.upload(session, wsSession, zkEntity.getRealPath(), getPath(olkConfig.getSsUploadPath(), zkEntity.getFileName()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.END_UPLOAD_ZOOKEEPER, opsHostEntity.getPublicIp()));
        wsUtil.sendText(wsSession, String.format(OlkProcessFlagStr.START_UPLOAD_RULE_YAML, opsHostEntity.getPublicIp()));
        jschUtil.upload(session, wsSession, new ByteArrayInputStream(
                olkConfig.getRuleYaml().getBytes(StandardCharsets.UTF_8)),
                getPath(olkConfig.getSsUploadPath(), "rule.yaml"));
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
            JschResult result = jschUtil.executeCommand("mkdir -p " + olkConfig.getOlkUploadPath(), session);
            if (result.getExitCode() != 0) {
                throw new OpsException(result.getResult());
            }
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
            JschResult result = jschUtil.executeCommand("mkdir -p " + olkConfig.getDadInstallPath(), session);
            if (result.getExitCode() != 0) {
                throw new OpsException(result.getResult());
            }
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
        wsUtil.sendText(wsSession, "Check if deployment service is running");
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
                    wsUtil.sendText(wsSession, "Deployment service start successfully, Now we can send command");
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
        wsUtil.sendText(wsSession, OlkProcessFlagStr.SEND_CMD_TO_DAD_SERVICE);
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

        // if code goes here, we have already monitor on log file
        String reqUrl = String.format("http://%s:%s/%s", host.getPublicIp(), config.getDadPort(), DadReqPath.DEPLOY);
        HttpUtils.PostResponse response = HttpUtils.sendPost(reqUrl, JSON.toJSONString(olkCommandDto));
        DadResult result = JSON.parseObject(response.getBody(), DadResult.class);
        String reqId = response.getHeader(DadReqPath.REQ_ID_KEY);
        if (StrUtil.isEmpty(reqId)) {
            processError(wsSession, "Send command to deployment service failed, Can't find req id");
        }
        if (result != null && result.isSuccess()) {
            ReadLogParam param = ReadLogParam.builder().serverDto(serverDto)
                    .logPath(config.getDadLogFileName()).olkEntity(config.toEntity())
                    .startFlag(OlkProcessFlagStr.START_DEPLOY)
                    .endFlag(OlkProcessFlagStr.END_DEPLOY)
                    .errFlag(OlkProcessFlagStr.END_DEPLOY_IN_ERROR)
                    .reqId(response.getHeader(DadReqPath.REQ_ID_KEY))
                    .wsSession(wsSession).build();
            threadReadLogToWs(param);
            wsUtil.sendText(wsSession, OlkProcessFlagStr.SEND_CMD_TO_SERVICE_COMPLETE);
        } else {
            processError(wsSession, "Send command to deployment service failed, error: " + result.get(DadResult.MSG_TAG));
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
        ruleDto.setColumn(config.getColumns());
        ruleDto.setTableName(config.getTableName());
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

    @Transactional
    public void saveToDb (OpsOlkEntity entity) {
        save(entity);
    }

    public void threadReadLogToWs(ReadLogParam param) {
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            BufferedReader reader = null;
            InputStream in = null;
            InputStreamReader isr = null;
            ChannelExec channel = null;
            Session session = null;
            ServerDto serverDto = param.getServerDto();
            try {
                // Read the log file line by line
                session = jschUtil.getSession(serverDto.getIp(), serverDto.getPort(), serverDto.getUsername(), serverDto.getPassword()).orElseThrow(() -> new OpsException("Connection establishment failed"));
                int startCount = 0;
                wsUtil.sendText(param.getWsSession(), String.format("Finding start flag \"%s\" in log file %s", param.getSF(), param.getLogPath()));
                while (true) {
                    String cmd = String.format("grep -n \"%s\" %s", param.getSF(), param.getLogPath());
                    JschResult result = jschUtil.executeCommand(cmd, session);
                    if (result.getExitCode() != 0 || StrUtil.isEmpty(result.getResult())) {
                        startCount++;
                        if (startCount > startLoopCount) {
                            throw new Exception("Can't find start log after " + startLoopCount * startLoopWaitTime + "s");
                        }
                        wsUtil.sendText(param.getWsSession(), String.format("%s st try to find start flag", startCount));
                        Thread.sleep(startLoopWaitTime * 1000);
                    } else {
                        wsUtil.sendText(param.getWsSession(), "Find start flag in log file: " + result.getResult());
                        break;
                    }
                }

                channel = (ChannelExec) session.openChannel("exec");
                channel.setPtyType("dump");
                channel.setPty(false);
                channel.setCommand(String.format("grep -n \"%s\" %s | tail -1 | awk -F \":\" '{print $1}' | xargs -I {} tail -f %s -n +{}", param.getSF(), param.getLogPath(), param.getLogPath()));
                channel.connect(20000);
                in = channel.getInputStream();
                isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                reader = new BufferedReader(isr);
                String line;
                TimeLimiter limiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());

                while (true) {
                    line = limiter.callWithTimeout(reader::readLine, readLogTimeout, TimeUnit.SECONDS);
                    if (line.contains(param.getEEE())) {
                        throw new Exception(line);
                    }
                    if (line.contains(param.getEF())) {
                        wsUtil.sendText(param.getWsSession(), line);
                        wsUtil.sendText(param.getWsSession(), OlkProcessFlagStr.END_DEPLOY_PROCESS);
                        wsUtil.setSuccessFlag(param.getWsSession());
                        if (param.getOlkEntity() != null) {
                            saveToDb(param.getOlkEntity());
                        }
                        return;
                    }
                    wsUtil.sendText(param.getWsSession(), line);
                }
            } catch (Exception ex) {
                String errMsg = String.format("Failed to Read process log %s, error: %s", param.getLogPath(), ex.getMessage());
                wsUtil.sendText(param.getWsSession(), errMsg);
                wsUtil.setErrorFlag(param.getWsSession());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("close input stream failed: " + e.getMessage());
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        log.error("close input stream reader failed: " + e.getMessage());
                    }
                }
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ioException) {
                    wsUtil.sendText(param.getWsSession(), "Close log reader error: " + ioException.getMessage());
                }
            }
        });

        TaskManager.registry(param.getWsSession().getSessionId(), future);
    }

    private void readLogToWs(ServerDto serverDto, String logPath, String reqId, WsSession wsSession) {
        try {
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

    public String getDadLogFileName(String dadInstallPath) {
        // return Path.of(dadInstallPath, DadReqPath.LOG_FILE_NAME + id).toString();
        return Path.of(dadInstallPath, DadReqPath.OUTPUT_LOG).toString().replace("\\", "/");
    }

    @Data
    @Builder
    private static class ReadLogParam {
        private ServerDto serverDto;
        private String startFlag;
        private String endFlag;
        private String errFlag;
        private String reqId;
        private String logPath;
        private WsSession wsSession;
        private OpsOlkEntity olkEntity;

        public String getSF() {
            return String.format("(%s) %s", reqId, startFlag);
        }

        public String getEF() {
            return String.format("(%s) %s", reqId, endFlag);
        }

        public String getEEE() {
            return String.format("(%s) %s", reqId, errFlag);
        }
    }
}
