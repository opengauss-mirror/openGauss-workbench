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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
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
        List<NctigbaEnvDO> mainPromList=
            envMapper.selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType,
                NctigbaEnvDO.envType.PROMETHEUS_MAIN));
        NctigbaEnvDO mainEnv = new NctigbaEnvDO();
        if (CollectionUtil.isNotEmpty(mainPromList)) {
            mainEnv = mainPromList.get(0);
        }
        try {
            // install the main Prometheus
            Path path = Path.of(MAIN_INSTALL_PATH);
            if (Files.exists(path)) {
                Files.createDirectories(path);
            }
            Resource resource = getMainInstallPkg();
            if (StrUtil.isBlank(mainEnv.getId())) {
                int port = checkPortAndGet(9090, 9099);
                String filename = resource.getFilename();
                String promDir = path.toAbsolutePath() + "/" + filename.substring(0, filename.length() - TAR.length());
                mainEnv.setPort(port).setPath(promDir)
                    .setType(NctigbaEnvDO.envType.PROMETHEUS_MAIN);
            }
            if (!Files.exists(Path.of(mainEnv.getPath()))) {
                // install
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
            List<NctigbaEnvDO> secondEnvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.PROMETHEUS));
            if (CollectionUtil.isEmpty(secondEnvList)) {
                return;
            }
            secondEnvList.forEach(item -> item.setHost(hostFacade.getById(item.getHostid())));
            updateMainPromConfig(mainEnv.getPath(), mainEnv.getPort(), secondEnvList, null);
        } catch (IOException | InterruptedException | CustomException e) {
            throw new CustomException(e.getMessage(),e);
        }
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
            String result = CommonUtils.processCommand("/bin/sh", "-c",
                "netstat -tuln | grep :" + port + " && echo true || echo false");
            isUsed = result.contains("true");
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

    private Resource getMainInstallPkg() throws IOException {
        String archStr = System.getProperty("os.arch");
        Resource[] resources =
            resourcePatternResolver.getResources("classpath*:pkg/prometheus-**-" + archStr + TAR);
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
        wsSessionStepTl.remove();
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
            if (envType.PROMETHEUS_MAIN.name().equals(promInstall.getType())) {
                updateMainPromInstall(promInstall);
                return;
            }
            boolean isNewInstall = StrUtil.isBlank(promInstall.getEnvId());
            nextStep(); // step2 check env
            List<NctigbaEnvDO> nctigbaEnvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS));
            if (CollectionUtil.isNotEmpty(nctigbaEnvList)
                && (isNewInstall || !nctigbaEnvList.get(0).getId().equals(promInstall.getEnvId()))) {
                throw new CustomException("The second server is exists,please refresh");
            }
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
            updateMainPromConfig(mainEnv.getPath(), mainEnv.getPort(), Arrays.asList(env), Arrays.asList(oldEnv));
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
            sshSession.executeNoWait("cd " + env.getPath() + " && sh " + RUN_SCRIPT + " start");
            updateMainPromConfig(mainEnv.getPath(), mainEnv.getPort(), Arrays.asList(env), null);
            // Circular waiting for Prometheus API connectivity
            nextStep(); // step6 check prom status
            checkHealthStatus(env);
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
            "netstat -tuln | grep :" + promInstall.getPort() + " && echo true || echo false");
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
        boolean isFileExist = sshSession.checkFileExist(path + "/" + filename);
        if (!isFileExist) {
            sshSession.upload(resource.getInputStream(), path + "/" + filename);
            sendMsg(null, "prominstall.uploadsuccess");
        } else {
            sendMsg(null, "prominstall.pkgexists");
        }
        String promDir = path + "/" + filename.substring(0, filename.length() - TAR.length());
        isFileExist = sshSession.checkDirExist(promDir);
        if (isFileExist) {
            throw new CustomException("The directory is exist: " + promDir);
        }
        // Extract file
        sshSession.execute("cd " + path + " && " + command.TAR.parse(resource.getFilename()));

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("port", env.getPort());
        paramMap.put("storageDays", promInstall.getStorageDays());
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
            List<String> agentIds = map.get(promId);
            List<PromAgentRelationDO> promAgentRels = promAgentRelationMapper.selectList(
                Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvPromId, promId));
            List<String> oldEnvAgentIds = promAgentRels.stream().map(item -> item.getEnvAgentId()).collect(
                Collectors.toList());
            NctigbaEnvDO envProm = envPromList.stream().filter(
                item -> item.getId().equals(promId)).findFirst().get();
            clearNotExistPromConf(envProm, oldEnvAgentIds);
            updatePromConf(promId, oldEnvAgentIds, agentIds);
        }
    }

    private void clearNotExistPromConf(NctigbaEnvDO envProm, List<String> envExistAgentIds) throws IOException {
        try (SshSessionUtils session = connect(envProm.getHostid(), envProm.getUsername())){
            String promYmlStr = session.execute("cat " + envProm.getPath() + CommonConstants.PROMETHEUS_YML);
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
                        if (item.getJob_name().startsWith(nodeId)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
                if (CollectionUtil.containsAll(newScrapeConf, scrapeConfigs)) {
                    return;
                }
                promCofig.setScrape_configs(newScrapeConf);
            }
            File prom = File.createTempFile("prom", ".tmp");
            FileUtil.appendUtf8String(YamlUtils.dump(promCofig), prom);
            // upload
            session.execute("rm " + envProm.getPath() + CommonConstants.PROMETHEUS_YML);
            session.upload(prom.getCanonicalPath(), envProm.getPath() + CommonConstants.PROMETHEUS_YML);
            Files.delete(prom.toPath());
            OpsHostEntity promeHost = hostFacade.getById(envProm.getHostid());
            String url = "http://" + promeHost.getPublicIp() + ":" + envProm.getPort() + "/-/reload";
            HttpUtil.post(url, "");
        }
    }

    private void updatePromConf(String promId, List<String> oldEnvAgentIds, List <String> newEnvAgentIds) {
        if (CollectionUtil.containsAll(newEnvAgentIds, oldEnvAgentIds)
            && CollectionUtil.containsAll(oldEnvAgentIds, newEnvAgentIds)) {
            return;
        }
        List<String> addEnvAgentIds = newEnvAgentIds.stream().filter(item -> !oldEnvAgentIds.contains(item)).collect(
            Collectors.toList());
        List<PrometheusConfigNodeDTO> addConfigNodes = getAddConfigNodes(addEnvAgentIds);
        List<String> delEnvAgentIds = oldEnvAgentIds.stream().filter(item -> !newEnvAgentIds.contains(item)).collect(
            Collectors.toList());
        List<String> delNodeIds = getDelNodeIds(delEnvAgentIds);
        collectTemplateNodeService.setPrometheusConfig(promId, addConfigNodes, delNodeIds);
        if (CollectionUtil.isNotEmpty(delEnvAgentIds)) {
            promAgentRelationMapper.delete(Wrappers.<PromAgentRelationDO>lambdaQuery()
                .in(PromAgentRelationDO::getEnvAgentId, delEnvAgentIds)
                .eq(PromAgentRelationDO::getEnvPromId, promId));
        }
        if (CollectionUtil.isNotEmpty(addEnvAgentIds)) {
            for (String addEnvAgentId : addEnvAgentIds) {
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
        List<PrometheusConfigNodeDTO> addConfigNodes = getAddConfigNodes(Arrays.asList(envAgent.getId()));
        collectTemplateNodeService.setPrometheusConfig(promId, addConfigNodes, null);
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
            List<String> delNodeIds = getDelNodeIds(Arrays.asList(envAgent.getId()));
            collectTemplateNodeService.setPrometheusConfig(promAgentRel.getEnvPromId(), new ArrayList<>(), delNodeIds);
            promAgentRelationMapper.deleteById(promAgentRel);
        }
    }

    private List<PrometheusConfigNodeDTO> getAddConfigNodes(List<String> addEnvAgentIds) {
        List<String> addNodeIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(addEnvAgentIds)) {
            List<AgentNodeRelationDO> list = agentNodeRelationService.list(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().in(AgentNodeRelationDO::getEnvId, addEnvAgentIds));
            addNodeIds = list.stream().map(item -> item.getNodeId()).collect(Collectors.toList());
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

    private void updateMainPromConfig(String path, int port, List<NctigbaEnvDO> secondPromList,
                                    List<NctigbaEnvDO> delList) throws IOException {
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Path configPath = Path.of(path + CommonConstants.PROMETHEUS_YML);
            Files.copy(configPath, os);
            Map map = YamlUtils.get().loadAs(new ByteArrayInputStream(os.toByteArray()), Map.class);
            prometheusConfig prometheusConfig = new prometheusConfig();
            BeanUtil.copyProperties(map, prometheusConfig);
            List<prometheusConfig.remoteRead> remoteReadList = prometheusConfig.getRemote_read();
            if (CollectionUtil.isEmpty(remoteReadList)) {
                remoteReadList = new ArrayList<>();
            }
            if (CollectionUtil.isNotEmpty(delList)) {
                for (NctigbaEnvDO env : delList) {
                    String url = "http://" + env.getHost().getPublicIp() + ":" + env.getPort() + "/api/v1/read";
                    prometheusConfig.remoteRead rr = new prometheusConfig.remoteRead();
                    rr.setUrl(url);
                    rr.setRead_recent(true);
                    if (remoteReadList.contains(rr)) {
                        remoteReadList.remove(rr);
                    }
                }
            }
            for (NctigbaEnvDO env : secondPromList) {
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
            String result = HttpUtil.post("http://" + LOCAL_IP + ":" + port + "/-/reload",
                new HashMap<>());
            if (StrUtil.isNotBlank(result)) {
                throw new CustomException("The main Prometheus reload fail");
            }
        } catch (IOException e) {
            throw new IOException(e);
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
    @Scheduled(fixedDelay = CommonConstants.MONITOR_CYCLE, timeUnit = TimeUnit.SECONDS)
    public void monitorStatus() {
        List<NctigbaEnvDO> envList = envMapper
            .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().in(NctigbaEnvDO::getType, envType.PROMETHEUS.name(),
                envType.PROMETHEUS_MAIN.name()));
        envList.forEach(e -> {
            if (e.getType().equals(envType.PROMETHEUS_MAIN.name())) {
                return;
            }
            e.setHost(hostFacade.getById(e.getHostid()));
        });
        for (NctigbaEnvDO env : envList) {
            try {
                String status = env.getStatus();
                if (StrUtil.isBlank(status) && envType.PROMETHEUS.name().equals(env.getType())) {
                    oldVersionAdapter(env);
                }
                if (AgentStatusEnum.MANUAL_STOP.getStatus().equals(status)) {
                    continue;
                }
                long updateTime = env.getUpdateTime() != null ? env.getUpdateTime().getTime()
                    : new Date().getTime() - 3 * CommonConstants.MONITOR_CYCLE * 1000L - 1000L;
                boolean isTimeout = new Date().getTime() - updateTime > 3 * CommonConstants.MONITOR_CYCLE * 1000L;
                if ((AgentStatusEnum.STARTING.getStatus().equals(status) || AgentStatusEnum.STOPPING.getStatus().equals(
                        status)) && !isTimeout) {
                    continue;
                }
                AgentExceptionVO check = checkPidStatus(env);
                if (!check.isUpStatus()) {
                    startProm(env);
                    if (envType.PROMETHEUS.name().equals(env.getType())) {
                        promAlloc();
                    }
                } else {
                    checkHealthStatus(env);
                }
            } catch (CustomException | IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String target = env.getPath() + (env.getPath().endsWith("/") ? "" : "/") + RUN_SCRIPT;
            String result = session.execute(" [ -e " + target + " ] && echo true || echo false");
            if (result.equals("false")) {
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
            String pid = session.execute("ss -tulpn | grep :" + env.getPort() + " | awk '{print $7}' "
                + "| awk -F \",\" '{print $2}' | awk -F \"=\" '{print $2}'");
            if (StrUtil.isNotBlank(pid)) {
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
                    "netstat -tuln | grep :" + env.getPort().toString() + " && echo true || echo false");
                if (result.contains("true")) {
                    throw new CustomException("port is exists!");
                }
                result = CommonUtils.processCommand("/bin/sh", "-c", "[ -e " + env.getPath() + " ] && echo true || echo "
                    + "false");
                if (result.contains("false")) {
                    throw new CustomException("package is not exists!");
                }
                env.setStatus(AgentStatusEnum.STARTING.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                CommonUtils.processCommand(new File(env.getPath()), "/bin/sh", RUN_SCRIPT, "start");
            } else {
                try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                    String message = session.execute(
                        "netstat -tuln | grep -q " + env.getPort() + " && echo 'true' || echo 'false'");
                    if (message.contains("true")) {
                        env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                        envMapper.updateById(env);
                        throw new CustomException("port is exists!");
                    }
                    String pkgPath = session.execute("[ -e " + env.getPath() + " ] && echo 'true' || echo 'false'");
                    if (pkgPath.contains("false")) {
                        env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                        envMapper.updateById(env);
                        throw new CustomException("package is not exists!");
                    }
                    env.setStatus(AgentStatusEnum.STARTING.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " start";
                    session.executeNoWait(command);
                }
            }
        } catch (IOException | InterruptedException | CustomException e) {
            log.error(e.getMessage());
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(e.getMessage());
        }
        checkHealthStatus(env);
    }

    private void stopProm(NctigbaEnvDO env) {
        AgentExceptionVO agentException = checkPidStatus(env);
        if (!agentException.isUpStatus()) {
            if (agentException.getExceptionInfo().contains("Prometheus is not running")) {
                killPid(env);
                return;
            }
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        env.setStatus(AgentStatusEnum.STOPPING.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            try {
                CommonUtils.processCommand(new File(env.getPath()), "/bin/sh", RUN_SCRIPT, "stop");
            } catch (IOException | InterruptedException e) {
                env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException(e.getMessage());
            }
        } else {
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " stop";
                session.executeNoWait(command);
            } catch (IOException e) {
                env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException(e.getMessage());
            }
        }
        agentException = checkPidStatus(env);
        if (agentException.isUpStatus()) {
            env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
    }

    private void killPid(NctigbaEnvDO env) {
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            try {
                String pid = CommonUtils.processCommand("/bin/sh", "-c", "ss -tulpn | grep :" + env.getPort()
                    + " | awk '{print $7}' | awk -F \",\" '{print $2}' | awk -F \"=\" '{print $2}'");
                if (StrUtil.isBlank(pid)) {
                    env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    throw new CustomException("Prometheus is not running");
                }
                CommonUtils.processCommand("/bin/sh", "-c", "kill " + pid);
            } catch (IOException | InterruptedException e) {
                throw new CustomException(e.getMessage());
            }
        } else {
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                String pid = session.execute("ss -tulpn | grep :" + env.getPort() + " | awk '{print $7}' "
                    + "| awk -F \",\" '{print $2}' | awk -F \"=\" '{print $2}'");
                if (StrUtil.isBlank(pid)) {
                    env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    throw new CustomException("Prometheus is not running");
                }
                session.execute(command.KILL.parse(pid));
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
        } catch (IOException | InterruptedException | NullPointerException e) {
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
        for (int i = 0; i < 11; i++) {
            try {
                String publicIp = "";
                if (StrUtil.isBlank(env.getHostid())) {
                    publicIp = LOCAL_IP;
                } else {
                    publicIp = env.getHost().getPublicIp();
                }
                String str = HttpUtil.get("http://" + publicIp + ":" + env.getPort()
                    + "/api/v1/status/runtimeinfo");
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
                ThreadUtil.sleep(3000L);
                if (i == 10) {
                    env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    throw new CustomException("Prometheus is unhealthy");
                }
            }
        }
        env.setStatus(AgentStatusEnum.NORMAL.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
    }

    /**
     * startup agent
     *
     * @param id envId
     */
    public void start(String id) {
        NctigbaEnvDO env = getPromEnvInfo(id);
        startProm(env);
        try {
            if (envType.PROMETHEUS.name().equals(env.getType())) {
                promAlloc();
            }
        } catch (IOException e) {
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
        stopProm(env);
        try {
            if (envType.PROMETHEUS.name().equals(env.getType())) {
                // realloc
                promAlloc();
            }
        } catch (IOException e) {
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
    }

    /**
     * reinstall
     *
     * @param promInstall PromInstallDTO
     */
    public void reinstall(PromInstallDTO promInstall) {
        try {
            nextStep();
            if (!envType.PROMETHEUS_MAIN.name().equals(promInstall.getType())) {
                throw new CustomException("only the main Prometheus allow to reinstall");
            }
            if (StrUtil.isBlank(promInstall.getEnvId())) {
                throw new CustomException("The main Prometheus is not exist!");
            }
            NctigbaEnvDO env = envMapper.selectById(promInstall.getEnvId());
            if (env == null) {
                throw new CustomException("The main Prometheus is not exist!");
            }
            nextStep();
            AgentExceptionVO agentExceptionVO = checkPidStatus(env);
            if (agentExceptionVO.isUpStatus()) {
                sendMsg(null, "prominstall.stopServer");
                stopProm(env);
            }
            prometheusConfig mainPromConf = getMainPromConf(env.getPath());
            transferRuleConfig(env.getPath() + CommonConstants.SLASH + RULE_PATH, RULE_PATH, false);
            sendMsg(null, "prominstall.clearFolder");
            clearInstallFolder(env);
            envMapper.deleteById(env);
            nextStep();
            Path path = Path.of(MAIN_INSTALL_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            String promDir = wsInstallMain(promInstall, mainPromConf);
            env = new NctigbaEnvDO().setPath(promDir).setPort(promInstall.getPort()).setType(envType.PROMETHEUS_MAIN);
            envMapper.insert(env);
            nextStep();
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
        prometheusConfig promConfig = new prometheusConfig();
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Path configPath = Path.of(path + CommonConstants.PROMETHEUS_YML);
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
        uploadMainScript(promDir, promInstall.getPort(), false);
        String config = YamlUtils.dump(promconfig);
        try (InputStream inConfig = new ByteArrayInputStream(config.getBytes(Charset.defaultCharset()))) {
            Files.copy(inConfig,
                Paths.get(promDir + "/prometheus.yml"), StandardCopyOption.REPLACE_EXISTING);
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

                @Data
                public static class Authorization {
                    private String type;
                    private String credentials_file;
                }

                @Data
                public static class Conf {
                    private List<String> targets;
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

            nextStep();
            OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
            if (hostEntity == null) {
                throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
            }
            env.setHost(hostEntity);
            if (StrUtil.isBlank(env.getStatus()) && envType.PROMETHEUS.name().equals(env.getType())) {
                oldVersionAdapter(env);
            }

            nextStep();
            AgentExceptionVO agentException = checkPidStatus(env);
            if (agentException.isUpStatus()) {
                nextStep();
                try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                    String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " stop";
                    session.executeNoWait(command);
                }
                NctigbaEnvDO mainEnv = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS_MAIN));
                AgentExceptionVO check = checkPidStatus(mainEnv);
                if (check.isUpStatus()) {
                    updateMainPromConfig(mainEnv.getPath(), mainEnv.getPort(), new ArrayList<>(),
                        Arrays.asList(env));
                }
                promAlloc();
            } else {
                skipStep();
            }
            clearInstallFolder(env);
            envMapper.deleteById(id);
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

    private void clearInstallFolder(NctigbaEnvDO env) throws IOException {
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            if (StrUtil.isNotBlank(env.getPath()) && Files.exists(Path.of(env.getPath()))) {
                FileUtils.deleteDirectory(new File(env.getPath()));
            }
        } else {
            try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                String command = "[ -e " + env.getPath() + " ] && echo 'true' || echo 'false'";
                String result = session.execute(command);
                if (StrUtil.isNotBlank(env.getPath()) && !result.contains("false")) {
                    String cd = "cd " + env.getPath() + " && ";
                    session.execute(cd + "rm -rf " + env.getPath());
                }
            }
        }
    }
}