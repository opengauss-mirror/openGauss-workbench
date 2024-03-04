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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.util.SshSessionUtils;
import com.nctigba.observability.instance.util.SshSessionUtils.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

@Service
@Slf4j
public class PrometheusService extends AbstractInstaller {
    public static final String PATH = "https://github.com/prometheus/prometheus/releases/download/v2.42.0/";
    public static final String NAME = "prometheus-2.42.0.linux-";
    private static final String CLASSPATH_SCRIPT = "classpath:sh/";
    private static final String RUN_SCRIPT = "run_prometheus.sh";
    private static final String LOCAL_IP = "127.0.0.1";

    private List<Step> installSteps;
    private List<Step> uninstallSteps;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    @Autowired
    private PromAgentRelationMapper promAgentRelationMapper;
    @Autowired
    private AgentNodeRelationService agentNodeRelationService;
    @Autowired
    private CollectTemplateNodeService collectTemplateNodeService;

    public void install(WsSession wsSession, PromInstallDTO promInstall) {
        initInstallSteps();
        int curr = 0;
        String path = promInstall.getPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
            promInstall.setPath(path);
        }
        try {
            curr = nextStep(wsSession, installSteps, curr); // step2 check env
            List<NctigbaEnvDO> nctigbaEnvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS));
            if (CollectionUtil.isNotEmpty(nctigbaEnvList)) {
                throw new CustomException("The second Prometheus is exists,please refresh");
            }
            OpsHostEntity hostEntity = hostFacade.getById(promInstall.getHostId());
            if (hostEntity == null) {
                throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
            }
            // check main Prometheus and Get it
            NctigbaEnvDO mainEnv = checkMainPromAndGet();
            curr = nextStep(wsSession, installSteps, curr); // step3 check host
            installSteps.get(curr).add("install.use", promInstall.getUsername()); // step3.n install user
            try (SshSessionUtils sshSession = connect(promInstall.getHostId(), promInstall.getUsername())) {
                curr = nextStep(wsSession, installSteps, curr); // step4 install Prometheus
                NctigbaEnvDO env = new NctigbaEnvDO().setHostid(promInstall.getHostId()).setPort(promInstall.getPort())
                    .setUsername(promInstall.getUsername()).setType(envType.PROMETHEUS).setHost(hostEntity);
                installProm(sshSession, wsSession, env, promInstall, curr);
                // run Prometheus script
                curr = nextStep(wsSession, installSteps, curr); // step5 startup prometheus
                sshSession.executeNoWait("cd " + env.getPath() + " && sh " + RUN_SCRIPT + " start");
                updatePromMainConfig(mainEnv.getPath(), mainEnv.getPort(), hostEntity.getPublicIp(),
                    promInstall.getPort());
                // Circular waiting for Prometheus API connectivity
                curr = nextStep(wsSession, installSteps, curr); // step6 check prom status
                checkHealthStatus(env);
                reallocatePromAgentConfig(); // Reassign agent
                curr = nextStep(wsSession, installSteps, curr); // step7 done
                sendMsg(wsSession, installSteps, curr, status.DONE);
            }
        } catch (Exception e) {
            installSteps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(installSteps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
    }

    private void initInstallSteps() {
        installSteps = Arrays.asList(
            new Step("prominstall.step1"),
            new Step("prominstall.step2"),
            new Step("prominstall.step3"),
            new Step("prominstall.step4"),
            new Step("prominstall.step5"),
            new Step("prominstall.step6"),
            new Step("prominstall.step7"));
    }

    private void initUninstallSteps() {
        uninstallSteps = Arrays.asList(
            new Step("promuninstall.step1"),
            new Step("promuninstall.step2"),
            new Step("promuninstall.step3"),
            new Step("promuninstall.step4"),
            new Step("promuninstall.step5"));
    }

    private void installProm(SshSessionUtils sshSession, WsSession wsSession, NctigbaEnvDO env,
                            PromInstallDTO promInstall, int curr) throws IOException {
        sshSession.testPortCanUse(env.getPort());
        String path = promInstall.getPath();
        boolean isDirExist = sshSession.checkDirExist(path);
        if (!isDirExist) {
            sshSession.execute("mkdir -p " + path);
        }

        Resource resource = getPromInstallPkg(sshSession);
        boolean isFileExist = sshSession.checkFileExist(path + "/" + resource.getFilename());
        if (!isFileExist) {
            sshSession.upload(resource.getInputStream(), path + "/" + resource.getFilename());
            addMsg(wsSession, installSteps, curr, "prominstall.uploadsuccess");
        } else {
            addMsg(wsSession, installSteps, curr, "prominstall.pkgexists");
        }
        // Extract file
        sshSession.execute("cd " + path + " && " + command.TAR.parse(resource.getFilename()));

        String filename = resource.getFilename();
        String promDir = path + filename.substring(0, filename.length() - TAR.length());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("port", env.getPort());
        paramMap.put("storageDays", promInstall.getStorageDays());
        uploadScript(sshSession, promDir, paramMap);
        // save env
        env.setPath(promDir);
        envMapper.insert(env);
    }

    private NctigbaEnvDO checkMainPromAndGet() {
        NctigbaEnvDO nctigbaEnvDO = envMapper.selectOne(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS_MAIN));
        if (nctigbaEnvDO == null) {
            throw new CustomException("The main Prometheus is not exists!");
        }
        String promMainPath = nctigbaEnvDO.getPath();
        AgentExceptionVO agentException = checkMainPromPidStatus(promMainPath);
        if (!agentException.isUpStatus()) {
            throw new CustomException("Main Prometheus error: " + agentException.getExceptionInfo());
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

    private void uploadScript(SshSessionUtils sshSession, String promDir, Map<String, Object> paramMap)
        throws IOException {
        Resource startSh = resourcePatternResolver.getResource(CLASSPATH_SCRIPT + RUN_SCRIPT);
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
            sshSession.upload(inStream, promDir + "/" + startSh.getFilename());
            inStream.close();
        }
    }

    /**
     * reallocatePromAgentConfig
     *
     * @throws IOException exception
     */
    public void reallocatePromAgentConfig() {
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

        Map<String, Integer> promAgentCountMap = preAlloc(envPromList, envAgentList);
        Map<String, List<String>> promAgentMap = doAlloc(envPromList, envAgentList, promAgentCountMap);

        // Update secondary Prometheus configuration file
        updatePromsConfig(promAgentMap);
    }

    private Map<String, Integer> preAlloc(List<NctigbaEnvDO> envPromList, List<NctigbaEnvDO> envAgentList) {
        int agentTotal = envAgentList.size();
        int promTotal = envPromList.size();
        int per = agentTotal / promTotal;
        int rest = agentTotal % promTotal;
        Map<String, Integer> promAgentCountMap = new HashMap<>();
        // Allocate number of agents for Prometheus
        for (NctigbaEnvDO nctigbaEnvDO : envPromList) {
            if (rest == 0) {
                promAgentCountMap.put(nctigbaEnvDO.getId(), per);
            } else {
                promAgentCountMap.put(nctigbaEnvDO.getId(), per + 1);
                rest--;
            }
        }
        return promAgentCountMap;
    }

    private Map<String, List<String>> doAlloc(List<NctigbaEnvDO> envPromList, List<NctigbaEnvDO> envAgentList,
                                            Map<String, Integer> promAgentCountMap) {
        List<String> envPromIdList = envPromList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<PromAgentRelationDO> list = promAgentRelationMapper.selectList(null);
        List<PromAgentRelationDO> promAgentRelList = list.stream().filter(
            item -> envPromIdList.contains(item.getEnvPromId())).collect(Collectors.toList());
        List<String> existEnvAgentIds =
            promAgentRelList.stream().map(item -> item.getEnvAgentId()).collect(Collectors.toList());
        List<String> unallocEnvAgentIds =
            envAgentList.stream().filter(item -> !existEnvAgentIds.contains(item.getId()))
                .map(item -> item.getId()).collect(Collectors.toList());
        Map<String, List<String>> promAgentMap = promAgentRelList.stream().collect(
            Collectors.groupingBy(PromAgentRelationDO::getEnvPromId,
                Collectors.mapping(PromAgentRelationDO::getEnvAgentId, Collectors.toList())));
        // Based on the allocation quantity, identify Prometheus instances lacking agents and move surplus agents
        // from over-allocated Prometheus instances to the pool for reassignment
        List<String> unallocEnvPromIds = new ArrayList<>();
        for (NctigbaEnvDO nctigbaEnvDO : envPromList) {
            List<String> list1 = promAgentMap.getOrDefault(nctigbaEnvDO.getId(), new ArrayList<>());
            Integer count = promAgentCountMap.get(nctigbaEnvDO.getId());
            if (list1.size() == count) {
                continue;
            }
            if (list1.size() < count) {
                unallocEnvPromIds.add(nctigbaEnvDO.getId());
                continue;
            }
            List<String> list2 = list1.subList(0, count);
            List<String> list3 = list1.subList(count, list1.size());
            promAgentMap.replace(nctigbaEnvDO.getId(), list2);
            unallocEnvAgentIds.addAll(list3);
        }
        // Supplement Prometheus instances lacking agents
        int index = 0;
        for (String unallocEnvPromId : unallocEnvPromIds) {
            List<String> list1 = promAgentMap.getOrDefault(unallocEnvPromId, new ArrayList<>());
            Integer count = promAgentCountMap.get(unallocEnvPromId);
            if (unallocEnvAgentIds.size() <= index + count - list1.size()) {
                list1.addAll(unallocEnvAgentIds.subList(index, unallocEnvAgentIds.size()));
                promAgentMap.put(unallocEnvPromId, list1);
                break;
            }
            list1.addAll(unallocEnvAgentIds.subList(index, index + count - list1.size()));
            promAgentMap.put(unallocEnvPromId, list1);
            index += count - list1.size();
        }
        return promAgentMap;
    }

    private void updatePromsConfig(Map<String, List<String>> promAgentMap) {
        for (Map.Entry<String, List<String>> promAgent : promAgentMap.entrySet()) {
            String envPromId = promAgent.getKey();
            List<String> envAgentIds = promAgent.getValue();
            // Determine if configuration update is needed
            List<PromAgentRelationDO> promAgentRels = promAgentRelationMapper.selectList(
                Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvPromId, envPromId));
            if (CollectionUtil.isEmpty(envAgentIds) && CollectionUtil.isEmpty(promAgentRels)) {
                continue;
            }
            List<String> oldEnvAgentIds = promAgentRels.stream().map(item -> item.getEnvAgentId()).collect(
                Collectors.toList());
            if (CollectionUtil.containsAll(oldEnvAgentIds, envAgentIds)
                && CollectionUtil.containsAll(envAgentIds, oldEnvAgentIds)) {
                continue;
            }

            // Update secondary Prometheus configuration
            List<String> addEnvAgentIds = envAgentIds.stream().filter(item -> !oldEnvAgentIds.contains(item)).collect(
                Collectors.toList());
            List<PrometheusConfigNodeDTO> addConfigNodes = getAddConfigNodes(addEnvAgentIds);

            List<String> delEnvAgentIds = oldEnvAgentIds.stream().filter(item -> !envAgentIds.contains(item)).collect(
                Collectors.toList());
            List<String> delNodeIds = getDelNodeIds(delEnvAgentIds);

            collectTemplateNodeService.setPrometheusConfig(envPromId, addConfigNodes, delNodeIds);

            // Update the database by deleting first and then adding
            if (CollectionUtil.isNotEmpty(delEnvAgentIds)) {
                promAgentRelationMapper.delete(Wrappers.<PromAgentRelationDO>lambdaQuery()
                    .in(PromAgentRelationDO::getEnvAgentId, delEnvAgentIds)
                    .eq(PromAgentRelationDO::getEnvPromId, envPromId));
            }
            if (CollectionUtil.isNotEmpty(addEnvAgentIds)) {
                for (String addEnvAgentId : addEnvAgentIds) {
                    PromAgentRelationDO relation = new PromAgentRelationDO();
                    relation.setEnvAgentId(addEnvAgentId).setEnvPromId(envPromId).setCreateTime(new Date());
                    promAgentRelationMapper.insert(relation);
                }
            }
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

    private void updatePromMainConfig(String path, int port, String secondPromIp, int secondPromPort)
        throws IOException {
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Path configPath = Path.of(path + "/prometheus.yml");
            Files.copy(configPath, os);
            Map map = YamlUtils.get().loadAs(new ByteArrayInputStream(os.toByteArray()), Map.class);
            prometheusConfig prometheusConfig = new prometheusConfig();
            BeanUtil.copyProperties(map, prometheusConfig);
            List<prometheusConfig.remoteRead> remoteReadList = prometheusConfig.getRemote_read();
            if (CollectionUtil.isEmpty(remoteReadList)) {
                remoteReadList = new ArrayList<>();
            }
            String url = "http://" + secondPromIp + ":" + secondPromPort + "/api/v1/read";
            prometheusConfig.remoteRead rr = new prometheusConfig.remoteRead();
            rr.setUrl(url);
            rr.setRead_recent(true);
            if (!remoteReadList.contains(rr)) {
                remoteReadList.add(rr);
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
            String status = env.getStatus();
            if (StrUtil.isBlank(status)) {
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
            try {
                AgentExceptionVO check = checkPidStatus(env);
                if (!check.isUpStatus()) {
                    startProm(env);
                } else {
                    checkHealthStatus(env);
                }
            } catch (CustomException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername());
            InputStream in =
                resourcePatternResolver.getResource(CLASSPATH_SCRIPT + RUN_SCRIPT).getInputStream()) {
            String result = session.execute(
                " [ -e " + env.getPath() + "/" + RUN_SCRIPT + " ] && echo true || echo false");
            if (result.equals("false")) {
                session.upload(in, env.getPath() + "/" + RUN_SCRIPT);
            }
            String pid = session.execute("lsof -ti :" + env.getPort());
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
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            startMainProm(env);
        } else {
            startSecondProm(env);
        }
        checkHealthStatus(env);
        if (envType.PROMETHEUS.name().equals(env.getType())) {
            reallocatePromAgentConfig();
        }
    }

    private void startMainProm(NctigbaEnvDO env) {
        try {
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
        } catch (IOException | InterruptedException | CustomException e) {
            log.error(e.getMessage());
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(e.getMessage());
        }
    }

    private void startSecondProm(NctigbaEnvDO env) {
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
        } catch (IOException | RuntimeException e) {
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException("exec failed:" + e.getMessage());
        }
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
        if (envType.PROMETHEUS.name().equals(env.getType())) {
            reallocatePromAgentConfig();
        }
    }

    private void killPid(NctigbaEnvDO env) {
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            try {
                String pid = CommonUtils.processCommand("/bin/sh", "-c", "lsof -t -i " + env.getPort().toString());
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
                String pid = session.execute("lsof -t -i " + env.getPort());
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
        if (envType.PROMETHEUS_MAIN.name().equals(env.getType())) {
            return checkMainPromPidStatus(env.getPath());
        } else {
            return checkSecondPromPidStatus(env.getHostid(), env.getUsername(), env.getPath());
        }
    }

    private AgentExceptionVO checkMainPromPidStatus(String path) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try {
            String result = CommonUtils.processCommand(new File(path), "/bin/sh", RUN_SCRIPT, "status");
            if (StrUtil.isBlank(result)) {
                result = "Run the script(run_prometheus.sh) fail!";
            }
            agentExceptionVO.setAgentStatus(result.contains("Prometheus is running with PID:"), result);
        } catch (IOException | InterruptedException | NullPointerException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    private AgentExceptionVO checkSecondPromPidStatus(String hostid, String username, String path) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(hostid, username)) {
            String cd = "cd " + path + " && ";
            String message = session.execute(cd + " sh " + RUN_SCRIPT + " status");
            agentExceptionVO.setAgentStatus(message.contains("Prometheus is running with PID"), message);
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    /**
     * checkHealthStatus
     *
     * @param env NctigbaEnvDO
     */
    public void checkHealthStatus(NctigbaEnvDO env) {
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
    }

    /**
     * stop agent
     *
     * @param id envId
     */
    public void stop(String id) {
        NctigbaEnvDO env = getPromEnvInfo(id);
        stopProm(env);
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

    public void uninstall(WsSession wsSession, String id) {
        initUninstallSteps();
        // @formatter:on
        var curr = 0;

        try {
            curr = nextStep(wsSession, uninstallSteps, curr);
            var env = envMapper.selectById(id);
            if (env == null)
                throw new RuntimeException("id not found");

            if (envType.PROMETHEUS_MAIN.equals(env.getType())) {
                throw new CustomException("The main Prometheus can not uninstall!");
            }

            curr = nextStep(wsSession, uninstallSteps, curr);
            OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
            if (hostEntity == null) {
                throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
            }
            env.setHost(hostEntity);
            if (StrUtil.isBlank(env.getStatus())) {
                oldVersionAdapter(env);
            }

            curr = nextStep(wsSession, uninstallSteps, curr);
            AgentExceptionVO agentException = checkSecondPromPidStatus(env.getHostid(), env.getUsername(),
                env.getPath());
            if (agentException.isUpStatus()) {
                curr = nextStep(wsSession, uninstallSteps, curr);
                try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
                    String command = "cd " + env.getPath() + " && sh " + RUN_SCRIPT + " stop";
                    session.executeNoWait(command);
                    reallocatePromAgentConfig();
                }
            } else {
                curr = skipStep(wsSession, uninstallSteps, curr);
            }
            envMapper.deleteById(id);
            sendMsg(wsSession, uninstallSteps, curr, status.DONE);
        } catch (Exception e) {
            uninstallSteps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(uninstallSteps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
    }
}