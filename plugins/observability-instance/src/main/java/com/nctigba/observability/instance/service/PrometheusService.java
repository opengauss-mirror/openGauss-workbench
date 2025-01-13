/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  PrometheusService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/PrometheusService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.observability.instance.service.allocate.AllocatorService;
import com.nctigba.observability.instance.model.dto.AllocateServerDTO;
import com.nctigba.observability.instance.enums.AgentStatusEnum;
import com.nctigba.observability.instance.mapper.PromAgentRelationMapper;
import com.nctigba.observability.instance.model.dto.PromInstallDTO;
import com.nctigba.observability.instance.model.dto.PrometheusConfigNodeDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateNodeDO;
import com.nctigba.observability.instance.model.entity.PromAgentRelationDO;
import com.nctigba.observability.instance.model.vo.AgentExceptionVO;
import com.nctigba.observability.instance.model.vo.AgentStatusVO;
import com.nctigba.observability.instance.util.CommonUtils;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import com.nctigba.observability.instance.util.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO.envType;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.Status;
import com.nctigba.observability.instance.util.SshSessionUtils;
import com.nctigba.observability.instance.util.SshSessionUtils.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.Data;

@Service
@Slf4j
public class PrometheusService extends AbstractInstaller {
    public static final String PATH = "https://github.com/prometheus/prometheus/releases/download/v2.42.0/";
    public static final String NAME = "prometheus-2.42.0.linux-";
    private static final String MAIN_INSTALL_PATH = "data/prometheus-main";
    private static final String RUN_SCRIPT = "run_prometheus.sh";
    private static final String SCRIPT_PATH = "classpath:sh/" + RUN_SCRIPT;
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String RULE_PATH = "rules/";

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    @Autowired
    private PromAgentRelationMapper promAgentRelationMapper;
    @Autowired
    private AgentNodeRelationService agentNodeRelationService;
    @Autowired
    private CollectTemplateNodeService collectTemplateNodeService;
    @Autowired
    private AllocatorService allocatorService;

    public void installMainProm() {
        NctigbaEnvDO mainEnv = getMainPromEnv();
        try {
            // install the main Prometheus
            String pathStr = StrUtil.isBlank(mainEnv.getPath()) ? MAIN_INSTALL_PATH : mainEnv.getPath();
            Path path = Path.of(pathStr);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                chmodFile(pathStr);
            }
            Resource resource = getMainInstallPkg();
            if (StrUtil.isBlank(mainEnv.getId())) {
                int port = checkPortAndGet(9090, 9099);
                String filename = resource.getFilename();
                String promDir = path.toAbsolutePath() + "/" + filename.substring(0, filename.length() - TAR.length());
                mainEnv.setPort(port).setPath(promDir)
                    .setType(NctigbaEnvDO.envType.PROMETHEUS_MAIN);
            }
            if (!Files.exists(Path.of(mainEnv.getPath())) || !Files.exists(Path.of(mainEnv.getPath()
                + "/prometheus.yml")) || !Files.exists(Path.of(mainEnv.getPath() + "/prometheus"))) {
                // install
                rmPath(mainEnv.getPath());
                uploadMainProm(resource, path);
                uploadMainScript(mainEnv.getPath(), mainEnv.getPort(), false);
                chmodFile(mainEnv.getPath());
            }
            if (StrUtil.isBlank(mainEnv.getId())) {
                // save env
                envMapper.insert(mainEnv);
            }
            AgentExceptionVO agentExceptionVO = checkPidStatus(mainEnv);
            if (!agentExceptionVO.isUpStatus()) {
                // startup
                CommonUtils.processCommand(new File(mainEnv.getPath()), "/bin/sh", RUN_SCRIPT, "start");
            }
            checkHealthStatus(mainEnv);
            updateMainPromRemoteReadConfig();
        } catch (IOException | InterruptedException | RuntimeException e) {
            throw new CustomException(e.getMessage(),e);
        }
    }

    private void rmPath(String path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("rm", "-rf", path);
        Process process = processBuilder.start();
        boolean isExit = process.waitFor(2L, TimeUnit.SECONDS);
        if (!isExit) {
            throw new CustomException("rm fail!");
        }
    }

    private NctigbaEnvDO getMainPromEnv() {
        List<NctigbaEnvDO> mainPromList=
            envMapper.selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType,
                NctigbaEnvDO.envType.PROMETHEUS_MAIN));
        NctigbaEnvDO mainEnv = new NctigbaEnvDO();
        if (CollectionUtil.isNotEmpty(mainPromList)) {
            mainEnv = mainPromList.get(0);
        }
        return mainEnv;
    }

    private void uploadMainScript(String promDir, int port, boolean isForce)
        throws IOException, InterruptedException {
        Resource startSh = resourcePatternResolver.getResource(SCRIPT_PATH);
        String target = promDir + (promDir.endsWith("/") ? "" : "/") + startSh.getFilename();
        if (Files.exists(Path.of(target))) {
            if (!isForce) {
                return;
            }
            Files.delete(Path.of(promDir + "/" + startSh.getFilename()));
        }
        try (InputStream inputStream = startSh.getInputStream();
             BufferedReader bufferedReader =
                 new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            StringBuilder strBuilder = new StringBuilder();
            String line = null;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("port", port);
            paramMap.put("storageDays", "7d");
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(CommonUtils.parseText(line, paramMap));
                strBuilder.append(System.lineSeparator());
            }
            ByteArrayInputStream inStream =
                new ByteArrayInputStream(strBuilder.toString().getBytes(Charset.defaultCharset()));
            File file = new File(promDir, startSh.getFilename());
            inStream.transferTo(new FileOutputStream(file));
            inStream.close();
        }
    }

    private void chmodFile(String file) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (Files.isDirectory(Path.of(file))) {
            processBuilder.command("chmod", "-R", "777", file);
        } else {
            processBuilder.command("chmod", "777", file);
        }
        Process process = processBuilder.start();
        boolean isExit = process.waitFor(2L, TimeUnit.SECONDS);
        if (!isExit) {
            throw new CustomException("chmod 777 fail!");
        }
    }

    private void uploadMainProm(Resource resource, Path targetPath) {
        try (InputStream fi = resource.getInputStream();
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {
            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                Path targetDirResolved = targetPath.resolve(entry.getName());
                Path newPath = targetDirResolved.normalize();

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Path parent = newPath.getParent();
                    if (parent != null && Files.notExists(parent)) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CustomException("install fail");
        }
    }

    private int checkPortAndGet(int formPort, int maxPort) throws IOException, InterruptedException {
        int port = formPort;
        // Is the port in use
        boolean isUsed = true;
        while (port <= maxPort) {
            isUsed = isPortUsed(port);
            if (isUsed) {
                port++;
                continue;
            }
            break;
        }
        if (isUsed) {
            port = formPort;
        }
        return port;
    }

    private boolean isPortUsed(Integer port) throws IOException, InterruptedException {
        String result = CommonUtils.processCommand("/bin/sh", "-c",
            String.format(CommonConstants.PORT_IS_EXIST, port));
        return result.contains("true");
    }

    private Resource getMainInstallPkg() throws IOException {
        String archStr = System.getProperty("os.arch");
        Resource[] resources =
            resourcePatternResolver.getResources("classpath*:pkg/prometheus-**-" + arch(archStr) + TAR);
        if (resources.length == 0) {
            throw new CustomException("The Prometheus install package is not exist");
        }
        return resources[0];
    }

    /**
     * install
     *
     * @param wsSession WsSession
     * @param promInstall PromInstallDTO
     */
    public void install(WsSession wsSession, PromInstallDTO promInstall) {
        initWsSessionStepTl(wsSession, initInstallSteps());
        install(promInstall);
        if (wsSessionStepTl.get() !=  null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * install
     *
     * @param promInstall PromInstallDTO
     */
    public void install(PromInstallDTO promInstall) {
        String path = promInstall.getPath();
        if (StrUtil.isNotBlank(path) && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            promInstall.setPath(path);
        }
        try {
            if (promInstall.getPort() <= 1024) {
                throw new CustomException("The port number must be greater than 1024.");
            }
            if (envType.PROMETHEUS_MAIN.name().equals(promInstall.getType())) {
                updateMainPromInstall(promInstall);
                return;
            }
            boolean isNewInstall = StrUtil.isBlank(promInstall.getEnvId());
            nextStep(); // step2 check env
            if (!isNewInstall) {
                updateSecondPromInstall(promInstall);
                return;
            }
            secondPromInstall(promInstall);
        } catch (Exception e) {
            sendMsg(Status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            log.error("install fail!", e);
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("install fail! " + e.getMessage());
            }
        }
    }

    private void updateSecondPromInstall(PromInstallDTO promInstall) throws IOException {
        NctigbaEnvDO env = getPromEnvInfo(promInstall.getEnvId());
        String storageDays = "";
        String param = env.getParam();
        if (StrUtil.isNotBlank(param)) {
            JSONObject pJson = new JSONObject(param);
            storageDays = pJson.getStr("storageDays");
        }
        if (promInstall.getPort().equals(env.getPort()) && promInstall.getStorageDays().equals(storageDays)) {
            done();
            return;
        }
        NctigbaEnvDO oldEnv = new NctigbaEnvDO();
        BeanUtil.copyProperties(env, oldEnv);
        // check main Prometheus and Get it
        NctigbaEnvDO mainEnv = checkMainPromAndGet();
        nextStep(); // step3 check host
        sendMsg(null, "install.use", env.getUsername()); // step3.n install user
        try (SshSessionUtils sshSession = connect(env.getHostid(), env.getUsername())) {
            nextStep(); // step4 install Prometheus
            // check new port
            if (!promInstall.getPort().equals(env.getPort())) {
                sshSession.testPortCanUse(promInstall.getPort());
            }
            AgentExceptionVO agentExceptionVO = checkPidStatus(env);
            if (agentExceptionVO.isUpStatus()) {
                sendMsg(null, "prominstall.stopServer");
                stopProm(env);
            }
            // update script
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("port", promInstall.getPort());
            paramMap.put("storageDays", promInstall.getStorageDays());
            uploadSecondScript(sshSession, env.getPath(), paramMap);
            //update env
            JSONObject paramJson = new JSONObject();
            paramJson.putOpt("storageDays", promInstall.getStorageDays());
            env.setPort(promInstall.getPort()).setParam(paramJson.toString());
            envMapper.updateById(env);
            // step5 startup prometheus
            nextStep();
            sendMsg(null, "prominstall.startServer");
            startProm(env);
            // Circular waiting for Prometheus API connectivity
            nextStep(); // step6 check prom status
            checkHealthStatus(env);
            nextStep(); // step7 done
            sendMsg(Status.DONE, "");
        }
    }

    private void secondPromInstall(PromInstallDTO promInstall) throws IOException {
        // check main Prometheus and Get it
        NctigbaEnvDO mainEnv = checkMainPromAndGet();
        nextStep(); // step3 check host
        OpsHostEntity hostEntity = hostFacade.getById(promInstall.getHostId());
        if (hostEntity == null) {
            throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
        }
        sendMsg(null, "install.use", promInstall.getUsername()); // step3.n install user
        try (SshSessionUtils sshSession = connect(promInstall.getHostId(), promInstall.getUsername())) {
            nextStep(); // step4 install Prometheus
            JSONObject param = new JSONObject();
            param.putOpt("storageDays", promInstall.getStorageDays());
            NctigbaEnvDO env = new NctigbaEnvDO().setHostid(promInstall.getHostId()).setPort(promInstall.getPort())
                .setUsername(promInstall.getUsername()).setType(envType.PROMETHEUS).setHost(hostEntity)
                .setParam(param.toString());
            installSecondProm0(sshSession, env, promInstall);
            // run Prometheus script
            nextStep(); // step5 startup prometheus
            sshSession.execute("cd " + env.getPath() + " && sh " + RUN_SCRIPT + " start");
            // Circular waiting for Prometheus API connectivity
            nextStep(); // step6 check prom status
            checkHealthStatus(env);
            updateMainPromRemoteReadConfig();
            promAlloc();
            nextStep(); // step7 done
            sendMsg(Status.DONE, "");
        }
    }

    private void updateMainPromInstall(PromInstallDTO promInstall) throws IOException, InterruptedException {
        nextStep(); // step2 check env
        if (StrUtil.isBlank(promInstall.getEnvId())) {
            throw new CustomException("The main Prometheus is not exist!");
        }
        NctigbaEnvDO env = envMapper.selectById(promInstall.getEnvId());
        if (env == null) {
            throw new CustomException("The main Prometheus is not exist!");
        }
        if (env.getPort().equals(promInstall.getPort())) {
            done();
            return;
        }
        nextStep(); // step3 check host
        skipStep(); // skip check host, and next to install Prometheus(update script)
        // check new port
        String result = CommonUtils.processCommand("/bin/sh", "-c",
            String.format(CommonConstants.PORT_IS_EXIST, env.getPort()));
        boolean isUsed = result.contains("true");
        if (isUsed) {
            throw new CustomException("The port is used:" + promInstall.getPort());
        }
        AgentExceptionVO agentExceptionVO = checkPidStatus(env);
        if (agentExceptionVO.isUpStatus()) {
            sendMsg(null, "prominstall.stopServer");
            stopProm(env);
        }
        uploadMainScript(env.getPath(), promInstall.getPort(), true);
        env.setPort(promInstall.getPort());
        envMapper.updateById(env);
        sendMsg(null, "prominstall.updatePort");
        nextStep();
        startProm(env);
        sendMsg(null, "prominstall.startServer");
        nextStep(); // step6 check prom status
        checkHealthStatus(env);
        updateMainPromRemoteReadConfig();
        nextStep(); // step7 done
        sendMsg(Status.DONE, "");
    }

    private List<Step> initInstallSteps() {
        return Arrays.asList(
            new Step("prominstall.step1"),
            new Step("prominstall.step2"),
            new Step("prominstall.step3"),
            new Step("prominstall.step4"),
            new Step("prominstall.step5"),
            new Step("prominstall.step6"),
            new Step("prominstall.step7"));
    }

    private List<Step> initUninstallSteps() {
        return Arrays.asList(
            new Step("promuninstall.step1"),
            new Step("promuninstall.step2"),
            new Step("promuninstall.step3"),
            new Step("promuninstall.step4"),
            new Step("promuninstall.step5"));
    }

    private List<Step> initReinstallSteps() {
        return Arrays.asList(
            new Step("prominstall.step1"),
            new Step("prominstall.step2"),
            new Step("prominstall.step8"),
            new Step("prominstall.step4"),
            new Step("prominstall.step5"),
            new Step("prominstall.step6"),
            new Step("prominstall.step7"));
    }

    private void installSecondProm0(SshSessionUtils sshSession, NctigbaEnvDO env, PromInstallDTO promInstall)
        throws IOException {
        sshSession.testPortCanUse(env.getPort());
        String path = promInstall.getPath();
        boolean isDirExist = sshSession.checkDirExist(path);
        if (!isDirExist) {
            sshSession.execute("mkdir -p " + path);
        }

        Resource resource = getPromInstallPkg(sshSession);
        String filename = resource.getFilename();
        boolean isSubFileExist = sshSession.isDirNotEmpty(path);
        if (isSubFileExist) {
            throw new CustomException("The directory is not empty: " + path);
        }
        boolean isFileExist = sshSession.checkDirExist(path + "/" + filename);
        if (!isFileExist) {
            sshSession.upload(resource.getInputStream(), path + "/" + filename);
            sendMsg(null, "prominstall.uploadsuccess");
        } else {
            sendMsg(null, "prominstall.pkgexists");
        }
        // Extract file
        sshSession.execute("cd " + path + " && " + command.TAR.parse(resource.getFilename()));

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("port", env.getPort());
        paramMap.put("storageDays", promInstall.getStorageDays());

        String promDir = path + "/" + filename.substring(0, filename.length() - TAR.length());
        uploadSecondScript(sshSession, promDir, paramMap);
        // save env
        env.setPath(promDir);
        envMapper.insert(env);
    }

    private NctigbaEnvDO checkMainPromAndGet() {
        NctigbaEnvDO nctigbaEnvDO = envMapper.selectOne(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS_MAIN));
        if (nctigbaEnvDO == null) {
            throw new CustomException("The main server is not exists!");
        }
        AgentExceptionVO agentException = checkPidStatus(nctigbaEnvDO);
        if (!agentException.isUpStatus()) {
            throw new CustomException("The main Prometheus error: " + agentException.getExceptionInfo());
        }
        return nctigbaEnvDO;
    }

    private Resource getPromInstallPkg(SshSessionUtils sshSession) throws IOException {
        String arch = sshSession.execute(command.ARCH);
        Resource[] resources = resourcePatternResolver.getResources(
            "classpath*:pkg/prometheus-**-" + arch(arch) + TAR);
        if (resources.length == 0) {
            throw new CustomException("The Prometheus install package is not exist");
        }
        return resources[0];
    }

    private void uploadSecondScript(SshSessionUtils sshSession, String promDir, Map<String, Object> paramMap)
        throws IOException {
        Resource startSh = resourcePatternResolver.getResource(SCRIPT_PATH);
        try (InputStream inputStream = startSh.getInputStream();
             BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            StringBuilder strBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(CommonUtils.parseText(line, paramMap));
                strBuilder.append(System.lineSeparator());
            }
            ByteArrayInputStream inStream =
                new ByteArrayInputStream(strBuilder.toString().getBytes(Charset.defaultCharset()));
            String target = promDir + (promDir.endsWith("/") ? "" : "/") + startSh.getFilename();
            sshSession.upload(inStream, target);
            inStream.close();
        }
    }

    private void promAlloc() throws IOException {
        List<NctigbaEnvDO> envPromList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS.name())
                .and(q -> q.isNull(NctigbaEnvDO::getStatus)
                    .or().eq(NctigbaEnvDO::getStatus, AgentStatusEnum.NORMAL.getStatus())));
        if (CollectionUtil.isEmpty(envPromList)) {
            return;
        }
        List<NctigbaEnvDO> envAgentList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.EXPORTER.name())
                .and(q -> q.isNull(NctigbaEnvDO::getStatus)
                    .or().eq(NctigbaEnvDO::getStatus, AgentStatusEnum.NORMAL.getStatus())));
        if (CollectionUtil.isEmpty(envAgentList)) {
            removePromConfig(envPromList);
            return;
        }
        List<String> agentEnvIds = envAgentList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<String> existAgentEnvIds = agentNodeRelationService.list(Wrappers.<AgentNodeRelationDO>lambdaQuery()
            .in(AgentNodeRelationDO::getEnvId, agentEnvIds)).stream().map(item -> item.getEnvId()).collect(
            Collectors.toList());
        envAgentList = envAgentList.stream().filter(item -> existAgentEnvIds.contains(item.getId()))
            .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(envAgentList)) {
            removePromConfig(envPromList);
            return;
        }
        List<AllocateServerDTO> promServerList = envPromList.stream().map(item -> {
            OpsHostEntity host = hostFacade.getById(item.getHostid());
            return new AllocateServerDTO().setId(item.getId()).setIp(host.getPublicIp()).setPort(item.getPort());
        }).collect(Collectors.toList());
        allocatorService.init(promServerList);
        List<AllocateServerDTO> agentServerList = envAgentList.stream().map(item -> {
            OpsHostEntity host = hostFacade.getById(item.getHostid());
            return new AllocateServerDTO().setId(item.getId()).setIp(host.getPublicIp()).setPort(item.getPort());
        }).collect(Collectors.toList());
        Map<String, List<String>> map = allocatorService.alloc(agentServerList);
        for (String promId : map.keySet()) {
            try {
                List<String> agentIds = map.get(promId);
                List<PromAgentRelationDO> promAgentRels = promAgentRelationMapper.selectList(
                    Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvPromId, promId));
                List<String> oldEnvAgentIds = promAgentRels.stream().map(item -> item.getEnvAgentId()).collect(
                    Collectors.toList());
                NctigbaEnvDO envProm = envPromList.stream().filter(
                    item -> item.getId().equals(promId)).findFirst().get();
                clearNotExistPromConf(envProm, oldEnvAgentIds);
                updatePromConf(promId, oldEnvAgentIds, agentIds);
            } catch (IOException e) {
                log.error("update prometheus and exporter relation fail: {}", e.getMessage());
                var sw = new StringWriter();
                try (var pw = new PrintWriter(sw);) {
                    e.printStackTrace(pw);
                }
                log.error(sw.toString());
            }
        }
        List<String> agentIds = envAgentList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<String> promIds = envPromList.stream().map(item -> item.getId()).collect(Collectors.toList());
        promAgentRelationMapper.delete(Wrappers.<PromAgentRelationDO>lambdaQuery()
            .in(PromAgentRelationDO::getEnvAgentId, agentIds).notIn(PromAgentRelationDO::getEnvPromId, promIds));
    }

    private void clearNotExistPromConf(NctigbaEnvDO envProm, List<String> envExistAgentIds) throws IOException {
        try (SshSessionUtils session = connect(envProm.getHostid(), envProm.getUsername())){
            String promYmlStr = session.execute("cat " + envProm.getPath() + CommonConstants.PROMETHEUS_YML);
            if (StrUtil.isBlank(promYmlStr)) {
                log.error("cat promethues.yml, result is empty");
            }
            prometheusConfig promCofig = YamlUtils.loadAs(promYmlStr, prometheusConfig.class);
            List<prometheusConfig.job> scrapeConfigs = promCofig.getScrape_configs();
            if (CollectionUtil.isEmpty(scrapeConfigs)) {
                return;
            }
            if (CollectionUtil.isEmpty(envExistAgentIds)) {
                promCofig.setScrape_configs(new ArrayList<>());
            } else {
                List<AgentNodeRelationDO> list = agentNodeRelationService.list(
                    Wrappers.<AgentNodeRelationDO>lambdaQuery().in(AgentNodeRelationDO::getEnvId, envExistAgentIds));
                List<String> nodeIds = list.stream().map(item -> item.getNodeId()).collect(Collectors.toList());
                List<prometheusConfig.job> newScrapeConf = scrapeConfigs.stream().filter(item -> {
                    for (String nodeId : nodeIds) {
                        if (item.getJob_name().startsWith(nodeId + "_")) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(scrapeConfigs) && CollectionUtil.containsAll(newScrapeConf,
                    scrapeConfigs)) {
                    return;
                }
                promCofig.setScrape_configs(newScrapeConf);
            }
            if (promCofig == null) {
                log.error("The promethues config is empty");
            }
            File prom = File.createTempFile("prom", ".tmp");
            FileUtil.appendUtf8String(YamlUtils.dump(promCofig), prom);
            // upload
            session.upload(prom.getCanonicalPath(), envProm.getPath() + CommonConstants.PROMETHEUS_YML);
            Files.delete(prom.toPath());
            OpsHostEntity promeHost = hostFacade.getById(envProm.getHostid());
            String url = "http://" + promeHost.getPublicIp() + ":" + envProm.getPort() + "/-/reload";
            HttpUtil.post(url, "");
        }
    }

    private void updatePromConf(String promId, List<String> oldEnvAgentIds, List<String> newEnvAgentIds) {
        List<PrometheusConfigNodeDTO> addConfigNodes = getAddConfigNodes(newEnvAgentIds);
        List<String> delNodeIds = getDelNodeIds(oldEnvAgentIds);
        collectTemplateNodeService.setPrometheusConfig(promId, addConfigNodes, delNodeIds);
        if (CollectionUtil.isNotEmpty(oldEnvAgentIds)) {
            promAgentRelationMapper.delete(Wrappers.<PromAgentRelationDO>lambdaQuery()
                .in(PromAgentRelationDO::getEnvAgentId, oldEnvAgentIds)
                .eq(PromAgentRelationDO::getEnvPromId, promId));
        }
        if (CollectionUtil.isNotEmpty(newEnvAgentIds)) {
            for (String addEnvAgentId : newEnvAgentIds) {
                PromAgentRelationDO relation = new PromAgentRelationDO();
                relation.setEnvAgentId(addEnvAgentId).setEnvPromId(promId).setCreateTime(new Date());
                promAgentRelationMapper.insert(relation);
            }
        }
    }

    private void removePromConfig(List<NctigbaEnvDO> envPromList) throws IOException {
        for (NctigbaEnvDO env : envPromList) {
            List<PromAgentRelationDO> promAgentRelList = promAgentRelationMapper.selectList(
                Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvPromId, env.getId()));
            if (CollectionUtil.isEmpty(promAgentRelList)) {
                continue;
            }
            clearNotExistPromConf(env, null);
            List<Integer> ids = promAgentRelList.stream().map(item -> item.getId()).collect(Collectors.toList());
            promAgentRelationMapper.deleteBatchIds(ids);
        }
    }

    /**
     * alloc prom config when increment an agent
     * @param envAgent NctigbaEnvDO
     */
    public void incAgentAlloc(NctigbaEnvDO envAgent) {
        List<NctigbaEnvDO> envPromList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS.name())
                .and(q -> q.isNull(NctigbaEnvDO::getStatus)
                    .or().eq(NctigbaEnvDO::getStatus, AgentStatusEnum.NORMAL.getStatus())));
        if (CollectionUtil.isEmpty(envPromList)) {
            return;
        }
        List<AllocateServerDTO> promServerList = envPromList.stream().map(item -> {
            OpsHostEntity host = hostFacade.getById(item.getHostid());
            return new AllocateServerDTO().setId(item.getId()).setIp(host.getPublicIp()).setPort(item.getPort());
        }).collect(Collectors.toList());
        allocatorService.init(promServerList);
        AllocateServerDTO agentServer = new AllocateServerDTO().setIp(envAgent.getId())
            .setIp(envAgent.getHost().getPublicIp()).setPort(envAgent.getPort());
        String promId = allocatorService.getAllocatorId(agentServer);
        allocatorService.remove();
        List<PrometheusConfigNodeDTO> addConfigNodes = getAddConfigNodes(Arrays.asList(envAgent.getId()));
        collectTemplateNodeService.setPrometheusConfig(promId, addConfigNodes, null);
        List<PromAgentRelationDO> relations = promAgentRelationMapper.selectList(
            Wrappers.<PromAgentRelationDO>lambdaQuery()
                .eq(PromAgentRelationDO::getEnvPromId, promId).eq(PromAgentRelationDO::getEnvAgentId,
                    envAgent.getId()));
        if (CollectionUtil.isNotEmpty(relations)) {
            return;
        }
        PromAgentRelationDO relation = new PromAgentRelationDO();
        relation.setEnvAgentId(envAgent.getId()).setEnvPromId(promId).setCreateTime(new Date());
        promAgentRelationMapper.insert(relation);
    }

    /**
     * del prom config when decrement an agent
     *
     * @param envAgent NctigbaEnvDO
     */
    public void decAgentAlloc(NctigbaEnvDO envAgent) {
        List<PromAgentRelationDO> promAgentRels = promAgentRelationMapper.selectList(
            Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvAgentId, envAgent.getId()));
        if (CollectionUtil.isEmpty(promAgentRels)) {
            return;
        }
        for (PromAgentRelationDO promAgentRel : promAgentRels) {
            NctigbaEnvDO envProm = envMapper.selectById(promAgentRel.getEnvPromId());
            if (envProm == null) {
                continue;
            }
            List<String> delNodeIds = getDelNodeIds(Arrays.asList(envAgent.getId()));
            collectTemplateNodeService.setPrometheusConfig(promAgentRel.getEnvPromId(), new ArrayList<>(), delNodeIds);
            promAgentRelationMapper.deleteById(promAgentRel);
        }
    }

    private List<PrometheusConfigNodeDTO> getAddConfigNodes(List<String> addEnvAgentIds) {
        Set<String> addNodeIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(addEnvAgentIds)) {
            List<AgentNodeRelationDO> list = agentNodeRelationService.list(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().in(AgentNodeRelationDO::getEnvId, addEnvAgentIds));
            addNodeIds = list.stream().map(item -> item.getNodeId()).collect(Collectors.toSet());
        }
        List<PrometheusConfigNodeDTO> addConfigNodes = new ArrayList<>();
        for (String addNodeId : addNodeIds) {
            CollectTemplateNodeDO collectTemplateNodeDO =
                collectTemplateNodeService.getOne(
                    new LambdaQueryWrapper<CollectTemplateNodeDO>()
                        .eq(CollectTemplateNodeDO::getNodeId, addNodeId)
                        .last("limit 1"));

            Integer templateId =
                collectTemplateNodeDO == null ? null : collectTemplateNodeDO.getTemplateId();

            List<PrometheusConfigNodeDTO> configNodes =
                collectTemplateNodeService.getNodePrometheusConfigParam(templateId, Arrays.asList(addNodeId));
            addConfigNodes.addAll(configNodes);
        }
        return addConfigNodes;
    }

    private List<String> getDelNodeIds(List<String> delEnvAgentIds) {
        List<String> delNodeIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(delEnvAgentIds)) {
            List<AgentNodeRelationDO> list = agentNodeRelationService.list(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().in(AgentNodeRelationDO::getEnvId, delEnvAgentIds));
            delNodeIds = list.stream().map(item -> item.getNodeId()).collect(Collectors.toList());
        }
        return delNodeIds;
    }

    private void updateMainPromRemoteReadConfig() {
        NctigbaEnvDO mainProm = getMainPromEnv();
        if (AgentStatusEnum.MANUAL_STOP.getStatus().equals(mainProm.getStatus())) {
            return;
        }
        List<NctigbaEnvDO> secondEnvList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.PROMETHEUS)
                .eq(NctigbaEnvDO::getStatus, AgentStatusEnum.NORMAL.getStatus()));
        if (CollectionUtil.isEmpty(secondEnvList)) {
            secondEnvList = new ArrayList<>();
        }
        secondEnvList.forEach(item -> item.setHost(hostFacade.getById(item.getHostid())));
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String path = mainProm.getPath();
            Path configPath = Path.of(path + CommonConstants.PROMETHEUS_YML);
            Files.copy(configPath, os);
            Map map = YamlUtils.get().loadAs(new ByteArrayInputStream(os.toByteArray()), Map.class);
            prometheusConfig prometheusConfig = new prometheusConfig();
            BeanUtil.copyProperties(map, prometheusConfig);
            List<prometheusConfig.remoteRead> remoteReadList = new ArrayList<>();
            for (NctigbaEnvDO env : secondEnvList) {
                String url = "http://" + env.getHost().getPublicIp() + ":" + env.getPort() + "/api/v1/read";
                prometheusConfig.remoteRead rr = new prometheusConfig.remoteRead();
                rr.setUrl(url);
                rr.setRead_recent(true);
                if (!remoteReadList.contains(rr)) {
                    remoteReadList.add(rr);
                }
            }
            prometheusConfig.setRemote_read(remoteReadList);
            String config = YamlUtils.dump(prometheusConfig);
            try (InputStream inConfig = new ByteArrayInputStream(config.getBytes(Charset.defaultCharset()))) {
                Files.copy(inConfig, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            Integer port = mainProm.getPort();
            String result = HttpUtil.post("http://" + LOCAL_IP + ":" + port + "/-/reload",
                new HashMap<>());
            if (StrUtil.isNotBlank(result)) {
                throw new CustomException("The main Prometheus reload fail");
            }
        } catch (IOException e) {
            throw new CustomException("updateMainPromRemoteReadConfig fail:" + e.getMessage());
        }
    }

    private SshSessionUtils connect(String hostId, String userName) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (hostEntity == null) {
            throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
        }
        var user = getUser(hostEntity, userName);
        return SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(), userName,
                encryptionUtils.decrypt(user.getPassword()));
    }

    /**
     * get all Prometheus status
     *
     * @return List<AgentStatusVO>
     */
    public List<AgentStatusVO> getStatus() {
        List<NctigbaEnvDO> envList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, envType.PROMETHEUS.name(),
                envType.PROMETHEUS_MAIN.name()));
        if (CollectionUtil.isEmpty(envList)) {
            return new ArrayList<>();
        }
        return envList.stream().map(env -> AgentStatusVO.of(env)).collect(Collectors.toList());
    }

    /**
     * monitorStatus
     */
    @Scheduled(fixedRate = CommonConstants.MONITOR_CYCLE, timeUnit = TimeUnit.SECONDS)
    public void monitorStatus() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<NctigbaEnvDO> envList = envMapper
            .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, envType.PROMETHEUS.name(),
                envType.PROMETHEUS_MAIN.name()).orderByDesc(NctigbaEnvDO::getStatus));
        boolean isToRealloc = false;
        for (NctigbaEnvDO env : envList) {
            try {
                env = envMapper.selectById(env.getId());
                if (env == null) {
                    continue;
                }
                if (env.getType().equals(envType.PROMETHEUS.name())) {
                    env.setHost(hostFacade.getById(env.getHostid()));
                }
                String oldStatus = env.getStatus();
                if (StrUtil.isBlank(oldStatus) && envType.PROMETHEUS.name().equals(env.getType())) {
                    oldVersionAdapter(env);
                }
                if (AgentStatusEnum.MANUAL_STOP.getStatus().equals(oldStatus)) {
                    continue;
                }
                long updateTime = env.getUpdateTime() != null ? env.getUpdateTime().getTime()
                    : new Date().getTime() - 3 * CommonConstants.MONITOR_CYCLE * 1000L - 1000L;
                boolean isTimeout = new Date().getTime() - updateTime > 3 * CommonConstants.MONITOR_CYCLE * 1000L;
                if ((AgentStatusEnum.STARTING.getStatus().equals(oldStatus) || AgentStatusEnum.STOPPING.getStatus().equals(
                    oldStatus)) && !isTimeout) {
                    continue;
                }
                AgentExceptionVO check = checkPidStatus(env);
                if (!check.isUpStatus()) {
                    startProm(env);
                } else {
                    env.setStatus(getHealthStatus(env) ? AgentStatusEnum.NORMAL.getStatus()
                        : AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                }
                String status = env.getStatus();
                if (AgentStatusEnum.NORMAL.getStatus().equals(status) && !isPromTimeSync(env)) {
                    env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date())
                        .setErrStatusMsg(MessageSourceUtils.get("promstatus.timesync"));
                    envMapper.updateById(env);
                }
                if ((AgentStatusEnum.NORMAL.getStatus().equals(oldStatus) && !AgentStatusEnum.NORMAL.getStatus().equals(status)
                    || !AgentStatusEnum.NORMAL.getStatus().equals(oldStatus) && AgentStatusEnum.NORMAL.getStatus().equals(status))
                    && !isToRealloc
                ) {
                    isToRealloc = true;
                }
            } catch (Exception e) {
                log.error("prometheus at {} is exception: {} ", env.getPath(), e.getMessage());
            }
        }
        if (isToRealloc) {
            try {
                promAlloc();
            } catch (Exception e) {
                log.error("prometheus alloc fail: {}", e.getMessage());
            }
        }
        stopWatch.stop();
        if (stopWatch.getTotalTimeSeconds() > CommonConstants.MONITOR_CYCLE) {
            log.error("Prometheus check status is over {}s, it takes {}s",
                CommonConstants.MONITOR_CYCLE, stopWatch.getTotalTimeSeconds());
        }
    }

    private boolean isPromTimeSync(NctigbaEnvDO env) {
        try {
            String publicIp = "";
            if (StrUtil.isBlank(env.getHostid())) {
                publicIp = LOCAL_IP;
            } else {
                publicIp = env.getHost().getPublicIp();
            }
            String str = HttpUtil.get("http://" + publicIp + ":" + env.getPort()
                + "/api/v1/query?query=time()", CommonConstants.HTTP_TIMEOUT);
            if (StrUtil.isBlank(str)) {
                return true;
            }
            JSONObject resJson = new JSONObject(str);
            if (!resJson.getStr("status").equals("success")) {
                return true;
            }
            JSONObject data = resJson.getJSONObject("data");
            if (CollectionUtil.isEmpty(data)) {
                return true;
            }
            JSONArray result = data.getJSONArray("result");
            if (CollectionUtil.isEmpty(result)) {
                return true;
            }
            Double promTime = result.getDouble(0);
            Double curTime = ((double) new Date().getTime()) / 1000;
            if (curTime - promTime >= 30.0) {
                log.error("The current time differs from the node time by {} seconds.", curTime - promTime);
                return false;
            }
        } catch (Exception e){
            log.error("get prometheus health status fail: {}", e.getMessage());
        }
        return true;
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String target = env.getPath() + (env.getPath().endsWith("/") ? "" : "/") + RUN_SCRIPT;
            if (!session.checkFileExist(target)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("port", env.getPort());
                String storageDays = "15d";
                if (StrUtil.isNotBlank(env.getParam())) {
                    JSONObject paramJson = new JSONObject(env.getParam());
                    storageDays = StrUtil.isNotBlank(paramJson.getStr("storageDays"))
                        ? paramJson.getStr("storageDays") : storageDays;
                }
                paramMap.put("storageDays", storageDays);
                uploadSecondScript(session, env.getPath(), paramMap);
            }
            String result = session.execute(String.format(CommonConstants.PORT_PID, env.getPort()));
            if (StrUtil.isNotBlank(result)) {
                String[] arr = result.split(System.lineSeparator());
                String pid = arr[arr.length - 1];
                if (!pid.matches("\\d+")) {
                    return;
                }
                File pidFile = File.createTempFile("prometheus", ".pid");
                FileUtil.appendUtf8String(pid, pidFile);
                session.upload(pidFile.getCanonicalPath(), env.getPath() + "/prometheus.pid");
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * startup Prometheus
     *
     * @param env NctigbaEnvDO
     */
    private void startProm(NctigbaEnvDO env) {
        AgentExceptionVO check = checkPidStatus(env);
        if (check.isUpStatus()) {
            throw new CustomException(check.getExceptionInfo());
        }
        try {
            if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
                String result = CommonUtils.processCommand("/bin/sh", "-c",
                    String.format(CommonConstants.PORT_IS_EXIST, env.getPort()));
                if (result.contains("true")) {
                    log.error(String.format(CommonConstants.PORT_IS_EXIST, env.getPort()));
                    log.error(result);
                    throw new CustomException("port(" + env.getPort() + ") is exists!");
                }
                result = CommonUtils.processCommand("/bin/sh", "-c", String.format(CommonConstants.DIRECTORY_IS_EXIST
                    , env.getPath()));
                if (result.contains("false")) {
                    log.error(String.format(CommonConstants.DIRECTORY_IS_EXIST, env.getPath()));
                    log.error(result);
                    throw new CustomException("package is not exists!");
                }
                env.setStatus(AgentStatusEnum.STARTING.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                CommonUtils.processCommand(new File(env.getPath()), "/bin/sh", RUN_SCRIPT, "start");
            } else {
                try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                    String message = session.execute(String.format(CommonConstants.PORT_IS_EXIST, env.getPort()));
                    if (message.contains("true")) {
                        env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                        envMapper.updateById(env);
                        throw new CustomException("port is exists!");
                    }
                    if (!session.checkDirExist(env.getPath())) {
                        env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                        envMapper.updateById(env);
                        throw new CustomException("package is not exists!");
                    }
                    env.setStatus(AgentStatusEnum.STARTING.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " start";
                    session.execute(command);
                }
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            log.error(e.getMessage());
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(e.getMessage());
        }
        checkHealthStatus(env);
        updateMainPromRemoteReadConfig();
    }

    private void stopProm(NctigbaEnvDO env) {
        AgentExceptionVO agentException = checkPidStatus(env);
        if (!agentException.isUpStatus()) {
            if (agentException.getExceptionInfo().contains("No such file or directory")) {
                killPid(env);
                env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                return;
            }
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        stoppingProm(env);
    }

    private void stoppingProm(NctigbaEnvDO env) {
        env.setStatus(AgentStatusEnum.STOPPING.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            try {
                CommonUtils.processCommand(new File(env.getPath()), "/bin/sh", RUN_SCRIPT, "stop");
            } catch (IOException | InterruptedException | RuntimeException e) {
                env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException(e.getMessage());
            }
        } else {
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " stop";
                session.execute(command);
            } catch (IOException | RuntimeException e) {
                env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException(e.getMessage());
            }
        }
        AgentExceptionVO agentException = checkPidStatus(env);
        if (agentException.isUpStatus()) {
            env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
        updateMainPromRemoteReadConfig();
    }

    private void killPid(NctigbaEnvDO env) {
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            try {
                String result = CommonUtils.processCommand("/bin/sh", "-c", String.format(CommonConstants.PORT_PID,
                    env.getPort()));
                if (StrUtil.isNotBlank(result)) {
                    String[] arr = result.split(System.lineSeparator());
                    String pid = arr[arr.length - 1];
                    if (!pid.matches("\\d+")) {
                        return;
                    }
                    CommonUtils.processCommand("/bin/sh", "-c", "kill " + pid);
                }
            } catch (IOException | InterruptedException | RuntimeException e) {
                throw new CustomException(e.getMessage());
            }
        } else {
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                String command = String.format(CommonConstants.PORT_PID, env.getPort());
                String result = session.execute(command);
                if (StrUtil.isNotBlank(result)) {
                    String[] arr = result.split(System.lineSeparator());
                    String pid = arr[arr.length - 1];
                    if (!pid.matches("\\d+")) {
                        return;
                    }
                    session.execute(SshSessionUtils.command.KILL.parse(pid));
                }
            } catch (IOException | RuntimeException e) {
                throw new CustomException(e.getMessage());
            }
        }

        env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
    }

    private AgentExceptionVO checkPidStatus(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try {
            if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
                String result = CommonUtils.processCommand(new File(env.getPath()), "/bin/sh", RUN_SCRIPT, "status");
                if (StrUtil.isBlank(result)) {
                    result = "Run the script(run_prometheus.sh) fail!";
                }
                agentExceptionVO.setAgentStatus(result.contains("Prometheus is running with PID:"), result);
            } else {
                try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                    String cd = "cd " + env.getPath() + " && ";
                    String message = session.execute(cd + " sh " + RUN_SCRIPT + " status");
                    agentExceptionVO.setAgentStatus(message.contains("Prometheus is running with PID"), message);
                }
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    /**
     * checkHealthStatus
     *
     * @param env NctigbaEnvDO
     */
    private void checkHealthStatus(NctigbaEnvDO env) {
        for (int i = 0; i < 31; i++) {
            try {
                String publicIp = "";
                if (StrUtil.isBlank(env.getHostid())) {
                    publicIp = LOCAL_IP;
                } else {
                    publicIp = env.getHost().getPublicIp();
                }
                String str = HttpUtil.get("http://" + publicIp + ":" + env.getPort()
                    + "/api/v1/status/runtimeinfo", CommonConstants.HTTP_TIMEOUT);
                if (StrUtil.isBlank(str)) {
                    throw new CustomException("HealthStatus: result is null!");
                }
                JSONObject resJson = new JSONObject(str);
                if (resJson.getStr("status").equals("success")) {
                    env.setStatus(AgentStatusEnum.NORMAL.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    break;
                }
                throw new CustomException("HealthStatus: result status is not success!");
            } catch (Exception e) {
                if (i == 30) {
                    env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    throw new CustomException("Prometheus is unhealthy");
                }
                ThreadUtil.sleep(2000L);
            }
        }
        env.setStatus(AgentStatusEnum.NORMAL.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
    }

    /**
     * getHealthStatus
     *
     * @param env NctigbaEnvDO
     * @return boolean
     */
    public boolean getHealthStatus(NctigbaEnvDO env) {
        try {
            String publicIp = "";
            if (StrUtil.isBlank(env.getHostid())) {
                publicIp = LOCAL_IP;
            } else {
                publicIp = env.getHost().getPublicIp();
            }
            String str = HttpUtil.get("http://" + publicIp + ":" + env.getPort()
                + "/api/v1/status/runtimeinfo", CommonConstants.HTTP_TIMEOUT);
            if (StrUtil.isBlank(str)) {
                return false;
            }
            JSONObject resJson = new JSONObject(str);
            if (resJson.getStr("status").equals("success")) {
                return true;
            }
        } catch (Exception e){
            log.error("get prometheus health status fail: {}", e.getMessage());
        }
        return false;
    }

    /**
     * startup agent
     *
     * @param id envId
     */
    public void start(String id) {
        NctigbaEnvDO env = getPromEnvInfo(id);
        if (StrUtil.isBlank(env.getStatus()) && envType.PROMETHEUS.name().equals(env.getType())) {
            oldVersionAdapter(env);
        }
        startProm(env);
        try {
            if (envType.PROMETHEUS.name().equals(env.getType())) {
                promAlloc();
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * stop agent
     *
     * @param id envId
     */
    public void stop(String id) {
        NctigbaEnvDO env = getPromEnvInfo(id);
        if (StrUtil.isBlank(env.getStatus()) && envType.PROMETHEUS.name().equals(env.getType())) {
            oldVersionAdapter(env);
        }
        stopProm(env);
        try {
            if (envType.PROMETHEUS.name().equals(env.getType())) {
                // realloc
                promAlloc();
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private NctigbaEnvDO getPromEnvInfo(String id) {
        NctigbaEnvDO env = envMapper.selectById(id);
        if (env == null) {
            throw new CustomException("Prometheus not found");
        }
        if (envType.PROMETHEUS.name().equals(env.getType())) {
            env.setHost(hostFacade.getById(env.getHostid()));
        }
        return env;
    }

    /**
     * reinstall
     *
     * @param wsSession WsSession
     * @param promInstall PromInstallDTO
     */
    public void reinstall(WsSession wsSession, PromInstallDTO promInstall) {
        initWsSessionStepTl(wsSession, initReinstallSteps());
        reinstall(promInstall);
        if (wsSessionStepTl.get() !=  null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * reinstall
     *
     * @param promInstall PromInstallDTO
     */
    public void reinstall(PromInstallDTO promInstall) {
        try {
            if (promInstall.getPort() <= 1024) {
                throw new CustomException("The port number must be greater than 1024.");
            }
            nextStep();
            if (!envType.PROMETHEUS_MAIN.name().equals(promInstall.getType())) {
                throw new CustomException("only the main Prometheus allow to reinstall");
            }
            if (StrUtil.isBlank(promInstall.getEnvId())) {
                throw new CustomException("The main Prometheus is not exist!");
            }
            nextStep();
            prometheusConfig mainPromConf = null;
            NctigbaEnvDO env = envMapper.selectById(promInstall.getEnvId());
            if (env != null) {
                AgentExceptionVO agentExceptionVO = checkPidStatus(env);
                if (agentExceptionVO.isUpStatus()) {
                    sendMsg(null, "prominstall.stopServer");
                    stopProm(env);
                }
                try {
                    mainPromConf = getMainPromConf(env.getPath());
                } catch (Exception e) {
                    log.error("Get the main prometheus configuration is fail: {}", e.getMessage());
                }
                transferRuleConfig(env.getPath() + CommonConstants.SLASH + RULE_PATH, RULE_PATH, false);
                sendMsg(null, "prominstall.clearFolder");
                clearInstallFolder(env);
                envMapper.deleteById(env);
            }
            nextStep();
            boolean isUsed = isPortUsed(promInstall.getPort());
            if (isUsed) {
                throw new CustomException("port(" + promInstall.getPort() + ") is used!");
            }
            Path path = Path.of(StrUtil.isNotBlank(promInstall.getPath()) ? promInstall.getPath() : MAIN_INSTALL_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            String promDir = wsInstallMain(promInstall, mainPromConf);
            env = new NctigbaEnvDO().setPath(promDir).setPort(promInstall.getPort()).setType(envType.PROMETHEUS_MAIN);
            envMapper.insert(env);
            nextStep();
            AgentExceptionVO agentExceptionVO = checkPidStatus(env);
            if (agentExceptionVO.isUpStatus()) {
                stopProm(env);
            }
            startProm(env);
            sendMsg(null, "prominstall.startServer");
            nextStep(); // step6 check prom status
            checkHealthStatus(env);
            nextStep(); // step7 done
            sendMsg(Status.DONE, "");
        } catch (Exception e) {
            sendMsg(Status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
        }
    }

    private void transferRuleConfig(String fromPath, String toPath, boolean isDelFromPath) throws IOException {
        if (StrUtil.isBlank(fromPath)) {
            return;
        }
        Path fromPath0 = Paths.get(fromPath);
        if (!Files.exists(fromPath0)) {
            return;
        }
        Path toPath0 = Paths.get(toPath);
        if (Files.exists(toPath0)) {
            FileUtil.del(toPath0);
        }
        Files.createDirectories(toPath0);
        Files.walk(fromPath0).forEach(file -> {
            try {
                Files.copy(file, toPath0.resolve(fromPath0.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new CustomException(e.getMessage());
            }
        });
        if (isDelFromPath) {
            FileUtil.del(fromPath0);
        }
    }

    private prometheusConfig getMainPromConf(String path) throws IOException {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        Path configPath = Path.of(path + CommonConstants.PROMETHEUS_YML);
        if (!Files.exists(configPath)) {
            return null;
        }
        prometheusConfig promConfig = new prometheusConfig();
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Files.copy(configPath, os);
            Map map = YamlUtils.get().loadAs(new ByteArrayInputStream(os.toByteArray()), Map.class);
            BeanUtil.copyProperties(map, promConfig);
        }
        return promConfig;
    }

    private String wsInstallMain(PromInstallDTO promInstall, prometheusConfig promconfig) throws IOException, InterruptedException {
        Resource resource = getMainInstallPkg();
        String filename = resource.getFilename().substring(0, resource.getFilename().length() - TAR.length());
        String installPath = "";
        String promDir = "";
        if (promInstall.getPath().endsWith(filename)) {
            installPath = promInstall.getPath().substring(0, promInstall.getPath().length() - filename.length());
            promDir = promInstall.getPath();
        } else {
            installPath = promInstall.getPath();
            promDir = promInstall.getPath() + "/" + filename;
        }
        uploadMainProm(resource, Path.of(installPath));
        uploadMainScript(promDir, promInstall.getPort(), true);
        if (promconfig != null) {
            String config = YamlUtils.dump(promconfig);
            try (InputStream inConfig = new ByteArrayInputStream(config.getBytes(Charset.defaultCharset()))) {
                Files.copy(inConfig,
                    Paths.get(promDir + "/prometheus.yml"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        transferRuleConfig(RULE_PATH, promDir + CommonConstants.SLASH + RULE_PATH, true);
        chmodFile(promDir);
        return promDir;
    }

    /**
     * default prometheus config
     *
     * @formatter:off alerting:
     * alertmanagers:
     * - static_configs:
     * - targets:
     * - alertmanager: 9093
     * global:
     * evaluation_interval: 15s
     * scrape_interval: 15s
     * rule_files: null
     * scrape_configs:
     * - job_name: prometheus
     * static_configs:
     * - targets:
     * - localhost:9090
     * @formatter:on
     */
    @Data
    public static class prometheusConfig {
        private global global;
        private alert alerting;
        private List<String> rule_files;
        private List<job> scrape_configs;
        private List<remoteRead> remote_read;

        @Data
        public static class alert {
            private List<Alertmanager> alertmanagers;

            @Data
            public static class Alertmanager {
                private String api_version;
                private String path_prefix;
                private String scheme = "http";
                private Authorization authorization;
                private List<Conf> static_configs;
                private Boolean follow_redirects;
                private Boolean enable_http2;
                private String timeout;
                private TlsConfig tls_config;

                @Data
                public static class Authorization {
                    private String type;
                    private String credentials_file;
                }

                @Data
                public static class Conf {
                    private List<String> targets;
                }

                @Data
                public static class TlsConfig {
                    private boolean insecure_skip_verify;
                }
            }
        }

        @Data
        public static class global {
            private String scrape_interval;
            private String evaluation_interval;
            private String scrape_timeout;
        }

        @Data
        public static class job {
            private String job_name;
            private List<conf> static_configs;
            private Boolean honor_timestamps;
            private String scrape_interval;
            private String scrape_timeout;
            private String metrics_path;
            private String scheme;
            private Boolean follow_redirects;
            private Boolean enable_http2;
            private Map<String, String[]> params;

            @Data
            public static class conf {
                private List<String> targets;
                private Map<String, String> labels;
            }

            @Override
            public int hashCode() {
                return job_name.hashCode();
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof job))
                    return false;
                var j = (job) o;
                return j.getJob_name().equals(this.job_name);
            }
        }

        @Data
        public static class remoteRead {
            private String url;
            private Boolean read_recent;
        }
    }

    /**
     * uninstall
     *
     * @param wsSession WsSession
     * @param id String
     */
    public void uninstall(WsSession wsSession, String id) {
        initWsSessionStepTl(wsSession, initUninstallSteps());
        uninstall(id);
        if (wsSessionStepTl.get() !=  null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * uninstall
     *
     * @param id String
     */
    public void uninstall(String id) {
        try {
            nextStep();
            NctigbaEnvDO env = envMapper.selectById(id);
            if (env == null) {
                throw new CustomException("env is not found");
            }
            if (envType.PROMETHEUS_MAIN.equals(env.getType())) {
                throw new CustomException("The main Prometheus can not uninstall!");
            }
            OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
            if (hostEntity == null) {
                throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
            }
            env.setHost(hostEntity);
            if (StrUtil.isBlank(env.getStatus()) && envType.PROMETHEUS.name().equals(env.getType())) {
                oldVersionAdapter(env);
            }
            nextStep();
            String oldStatus = env.getStatus();
            AgentExceptionVO agentException = checkPidStatus(env);
            if (!agentException.isUpStatus()) {
                if (agentException.getExceptionInfo().contains("No such file or directory")) {
                    killPid(env);
                    env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    updateMainPromRemoteReadConfig();
                }
            } else {
                stoppingProm(env);
            }
            if (AgentStatusEnum.NORMAL.getStatus().equals(oldStatus)) {
                promAlloc();
            }
            nextStep();
            clearInstallFolder(env);
            nextStep();
            envMapper.deleteById(id);
            sendMsg(Status.DONE, "");
        } catch (Exception e) {
            sendMsg(Status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            log.error("uninstall fail! {}", e.getMessage());
            log.error(sw.toString());
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("uninstall fail! " + e.getMessage());
            }
        }
    }

    private void clearInstallFolder(NctigbaEnvDO env) throws IOException, InterruptedException {
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            if (StrUtil.isNotBlank(env.getPath()) && Files.exists(Path.of(env.getPath()))) {
                FileUtils.deleteDirectory(new File(env.getPath()));
            }
        } else {
            if (StrUtil.isBlank(env.getPath())) {
                return;
            }
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                int index = env.getPath().lastIndexOf("/");
                String path = env.getPath().substring(0, index);
                if (StrUtil.isNotBlank(path) && session.checkDirExist(path)) {
                    String cd = "cd " + path + " && ";
                    session.execute(cd + "rm -rf " + path);
                }
            }
        }
    }
}