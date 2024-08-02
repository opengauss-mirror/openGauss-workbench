
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
 *  PrometheusServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/PrometheusServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import com.nctigba.alert.monitor.config.property.AlertProperty;
import com.nctigba.alert.monitor.config.property.AlertmanagerProperty;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertRuleConfigDTO;
import com.nctigba.alert.monitor.model.dto.PrometheusConfigDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.service.AlertConfigService;
import com.nctigba.alert.monitor.service.PrometheusService;
import com.nctigba.alert.monitor.util.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.common.exception.base.BaseException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used for requesting Prometheus data or modifying its configuration.
 *
 * @author wuyuebin
 * @since 2023/5/19 09:50
 */
@Service
@Slf4j
public class PrometheusServiceImpl implements PrometheusService {
    private final String AND = " and ";
    private final String OR = " or ";

    private NctigbaEnvDO env;
    private List<AlertConfigDO> alertConfigList;

    @Autowired
    private AlertTemplateRuleMapper alertTemplateRuleMapper;

    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Autowired
    private AlertClusterNodeConfMapper clusterNodeConfMapper;

    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    private AlertProperty alertProperty;

    @Autowired
    private AlertConfigService alertConfigService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    /**
     * init
     */
    public void init() {
        alertConfigList = alertConfigService.list();
        env = envMapper.selectOne(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.Type.PROMETHEUS_MAIN));
        if (env == null) {
            throw new ServiceException("uninstall the prometheus");
        }
        if (StrUtil.isBlank(env.getPath())) {
            throw new ServiceException("uninstall the prometheus");
        }
    }

    private PrometheusConfigDTO getPromConfig() {
        String promYmlStr = "";
        String url = "http://" + CommonConstants.LOCAL_IP + ":" + env.getPort() + "/api/v1/status/config";
        String result = HttpUtil.get(url);
        if (StrUtil.isNotBlank(result)) {
            JSONObject resultJson = new JSONObject(result);
            String status = resultJson.getStr("status", "");
            if (status.equals("success")) {
                JSONObject data = resultJson.getJSONObject("data");
                promYmlStr = data.getStr("yaml");
            }
        }
        HashMap map = YamlUtils.loadAs(promYmlStr, HashMap.class);
        if (map == null) {
            throw new NullPointerException("the prometheus config is null");
        }
        return BeanUtil.fillBeanWithMap(map, new PrometheusConfigDTO(), false);
    }
    private PrometheusConfigDTO.Alert.Alertmanager createAlertmanager() {
        List<PrometheusConfigDTO.Alert.Alertmanager.Conf> staticConfigs = new ArrayList<>();
        PrometheusConfigDTO.Alert.Alertmanager.Conf conf = new PrometheusConfigDTO.Alert.Alertmanager.Conf();
        List<String> targets = alertConfigList.stream().map(
            item -> item.getAlertIp() + ":" + item.getAlertPort()).collect(Collectors.toList());
        conf.setTargets(targets);
        staticConfigs.add(conf);

        AlertmanagerProperty alertmanagerPro = alertProperty.getAlertmanager();
        String apiVersion = alertmanagerPro.getApiVersion();
        String pathPrefix = alertmanagerPro.getPathPrefix();
        PrometheusConfigDTO.Alert.Alertmanager alertmanager = new PrometheusConfigDTO.Alert.Alertmanager();
        alertmanager.setPathPrefix(pathPrefix);
        alertmanager.setApiVersion(apiVersion);
        alertmanager.setStaticConfigs(staticConfigs);
        String http = "http";
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        if (environmentProvider != null) {
            Boolean bool = environmentProvider.getBoolean("server.ssl.enabled");
            http = bool != null && bool ? "https" : "http";
        }
        PrometheusConfigDTO.Alert.Alertmanager.TlsConfig tlsConfig =
            new PrometheusConfigDTO.Alert.Alertmanager.TlsConfig();
        tlsConfig.setInsecureSkipVerify(true);
        alertmanager.setTlsConfig(tlsConfig);
        alertmanager.setScheme(http);
        alertmanager.setIsFollowRedirects(true);
        alertmanager.setTimeout("10s");
        alertmanager.setIsEnableHttp2(true);
        return alertmanager;
    }
    private PrometheusConfigDTO createUpdatePromConfig(PrometheusConfigDTO config) {
        PrometheusConfigDTO cloneConfig = ObjectUtil.clone(config);
        PrometheusConfigDTO.Alert alerting = cloneConfig.getAlerting();
        alerting.setAlertmanagers(Arrays.asList(createAlertmanager()));
        String ruleFilePrefix = alertProperty.getRuleFilePrefix();
        String ruleFileSuffix = alertProperty.getRuleFileSuffix();
        String ruleFilePath = ruleFilePrefix + "*" + ruleFileSuffix;
        if (cloneConfig.getRuleFiles() == null) {
            cloneConfig.setRuleFiles(new ArrayList<>());
        }
        cloneConfig.getRuleFiles().add(ruleFilePath);
        cloneConfig.setRuleFiles(new ArrayList<>(new LinkedHashSet<>(cloneConfig.getRuleFiles())));
        PrometheusConfigDTO.Global global = cloneConfig.getGlobal();
        if (global == null) {
            global = new PrometheusConfigDTO.Global();
        }
        global.setEvaluationInterval(CommonConstants.EVALUATION_INTERVAL);
        cloneConfig.setGlobal(global);
        return cloneConfig;
    }
    private synchronized void uploadConfigFile(Map config, String path, String fileName) throws IOException {
        File configFile = File.createTempFile("prom", ".tmp");
        FileUtil.appendUtf8String(YamlUtils.dump(config), configFile);
        Files.copy(configFile.toPath(), Paths.get(path).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(configFile.toPath());
        String res = HttpUtil.post(
            "http://" + CommonConstants.LOCAL_IP + ":" + env.getPort() + "/-/reload", "");
        log.info(res);
        if (StrUtil.isNotBlank(res)) {
            throw new ServiceException("reload prometheus fail!");
        }
    }

    /**
     * When the program starts, it initializes the Prometheus configuration
     */
    public void initPrometheusConfig() {
        init();
        if (CollectionUtil.isEmpty(alertConfigList)) {
            return;
        }
        try {
            PrometheusConfigDTO config = getPromConfig();
            PrometheusConfigDTO updateConfig = createUpdatePromConfig(config);
            if (config.equals(updateConfig)) {
                return;
            }
            Map configMap = new ObjectMapper().convertValue(updateConfig, HashMap.class);
            uploadConfigFile(configMap, env.getPath(), CommonConstants.PROMETHEUS_YML);
        } catch (IOException | CryptoException | ServiceException | NullPointerException | BaseException
                | IORuntimeException e) {
            log.warn("init prometheus configuration fail: {}", e.getMessage());
        }
    }

    /**
     * updatePromConfigByAlertConfig
     *
     * @param alertConfigDO alertConfig
     */
    public synchronized void updatePrometheusConfig(AlertConfigDO alertConfigDO) {
        init();
        try {
            if (StrUtil.isBlank(alertConfigDO.getAlertIp()) && StrUtil.isBlank(alertConfigDO.getAlertPort())) {
                throw new ServiceException("The alert IP or the alert Port is empty!");
            }
            alertConfigList = Arrays.asList(alertConfigDO);

            PrometheusConfigDTO config = getPromConfig();
            PrometheusConfigDTO updateConfig = createUpdatePromConfig(config);
            if (config.equals(updateConfig)) {
                return;
            }
            Map configMap = new ObjectMapper().convertValue(updateConfig, HashMap.class);
            uploadConfigFile(configMap, env.getPath(), CommonConstants.PROMETHEUS_YML);
        } catch (IOException | NullPointerException | IORuntimeException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert config fail");
        }
    }

    /**
     * Get data from a Prometheus server based on promQL and time
     *
     * @param url http://IP:9090/api/v1/query_range?
     * @param port 9090
     * @param query query=promQL &start=1683316800 &end=1683489600 &step=691
     *
     *              step = Math.max(Math.floor(60 * 60 * 1000 / 250000), 1)
     *              Default of one hour,this.props.options.range=60 * 60 * 1000
     *
     * @param startTime startTime = endTime - this.props.options.range / 1000
     * @param endTime end = this.getEndTime().valueOf() / 1000
     * @return {"status":"success","data":{"resultType":"matrix",
     *        "result":[{"metric":{"instance":"6bd124fa-4a28-4f29-82a9-a81c880a5b32"},
     *        "values":[[1683472020,"1.5090909090907871"],[1683472365,"1.418181818182191"],
     *        [1683472710,"1.418181818182191"],[1683473055,"1.5090909090907871"],[1683473400,"1.4909090909085592"],[
     *        1683473745,"1.3454545454549844"],[1683474090,"1.3272727272727707"],[1683474435,"1.5454545454535662"],
     *        [1683474780,"1.2181818181819466"],[1683475125,"1.2545454545463883"],[1683475470,"1.399999999999153"],
     *        [1683475815,"1.87272727272682"],[1683476160,"1.836363636364041"],[1683476505,"100"],[1683476850,"100"],
     *        [1683477195,"1.45454545454578"],[1683477540,"1.5636363636374284"],[1683477885,"1.5454545454535662"],
     *        [1683478230,"1.3272727272735807"],[1683478575,"1.7454545454546206"],[1683478920,"1.472727272728008"],
     *        [1683479265,"1.4909090909085592"]]}]}}
     */
    public List queryRange(
        String url, String port, String query, LocalDateTime startTime,
        LocalDateTime endTime) {
        Instant startInstant = startTime.toInstant(ZoneOffset.of("+8"));
        Instant endInstant = endTime.toInstant(ZoneOffset.of("+8"));
        int step = Math.max(Long.valueOf((endInstant.toEpochMilli() - startInstant.toEpochMilli()) / 250000).intValue(),
            1);
        long start = startInstant.getEpochSecond();
        long end = endInstant.getEpochSecond();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("query", query);
        paramMap.put("start", start);
        paramMap.put("end", end);
        paramMap.put("step", step);
        String httpUrl = "http://" + url + ":" + port + "/api/v1/query_range";
        String res = HttpUtil.get(httpUrl, paramMap);
        if (StrUtil.isBlank(res)) {
            return new JSONArray();
        }
        JSONObject resJson = new JSONObject(res);
        if (!resJson.getStr("status", "").equals("success")) {
            return new JSONArray();
        }
        JSONObject data = resJson.getJSONObject("data");
        if (data == null) {
            return new JSONArray();
        }
        JSONArray result = data.getJSONArray("result");
        if (CollectionUtil.isEmpty(result)) {
            return new JSONArray();
        }
        return result;
    }

    /**
     * use to update the prometheus rule config
     *
     * @param ruleConfigMap the key is templateId,the value is clusterNodeIds
     */
    public synchronized void updateRuleConfig(Map<Long, String> ruleConfigMap) {
        if (CollectionUtil.isEmpty(ruleConfigMap)) {
            return;
        }
        init();
        for (Long templateId : ruleConfigMap.keySet()) {
            updateRuleConfig(templateId, ruleConfigMap.get(templateId));
        }
    }

    private Optional<AlertRuleConfigDTO> getAlertRuleConfigDto(Long templateId)
        throws IOException {
        String ruleDir = alertProperty.getRuleFilePrefix().endsWith(CommonConstants.SLASH)
            ? alertProperty.getRuleFilePrefix() : (alertProperty.getRuleFilePrefix() + CommonConstants.SLASH);
        String dir = env.getPath() + CommonConstants.SLASH + ruleDir;
        String fileName = "rule_template_" + templateId + alertProperty.getRuleFileSuffix();
        Path filePath = Paths.get(dir + CommonConstants.SLASH + fileName);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        String fileContent = new String(Files.readAllBytes(filePath), Charset.defaultCharset());
        Map map = YamlUtils.loadAs(fileContent, HashMap.class);
        if (CollectionUtil.isEmpty(map)) {
            return Optional.empty();
        }
        return Optional.of(BeanUtil.fillBeanWithMap(map, new AlertRuleConfigDTO(), false));
    }
    private void uploadRuleConfig(AlertRuleConfigDTO config, Long templateId) throws IOException {
        String dir = env.getPath() + CommonConstants.SLASH + alertProperty.getRuleFilePrefix()
            + (alertProperty.getRuleFilePrefix().endsWith("/") ? "" : "/");
        String fileName = "rule_template_" + templateId + alertProperty.getRuleFileSuffix();
        String path = dir + fileName;
        if (CollectionUtil.isEmpty(config.getGroups())) {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                return;
            }
            Files.delete(filePath);
            String res = HttpUtil.post("http://" + CommonConstants.LOCAL_IP + ":" + env.getPort()
                + "/-/reload", "");
            if (StrUtil.isNotBlank(res)) {
                throw new ServiceException("reload prometheus fail!");
            }
            return;
        }
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        Map configMap = new ObjectMapper().convertValue(config, HashMap.class);
        uploadConfigFile(configMap, dir, fileName);
    }

    private AlertRuleConfigDTO.Group createRuleGroup(AlertTemplateRuleDO alertTemplateRuleDO, String instances) {
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = alertTemplateRuleDO.getAlertRuleItemList();
        if (CollectionUtil.isEmpty(alertTemplateRuleItemDOS)) {
            alertTemplateRuleItemDOS = alertTemplateRuleItemMapper.selectList(
                Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().eq(AlertTemplateRuleItemDO::getTemplateRuleId,
                    alertTemplateRuleDO.getId()));
        }
        String ruleExpComb = alertTemplateRuleDO.getRuleExpComb();
        ruleExpComb = parseRuleExpComb(ruleExpComb, alertTemplateRuleItemDOS, instances);
        AlertRuleConfigDTO.Group group = new AlertRuleConfigDTO.Group();
        String name = "rule_" + alertTemplateRuleDO.getId();
        group.setName(name);
        AlertRuleConfigDTO.Group.Rule rule = new AlertRuleConfigDTO.Group.Rule();
        rule.setAliasFor(alertTemplateRuleDO.getNotifyDuration() + alertTemplateRuleDO.getNotifyDurationUnit());
        rule.setAlert(name);
        rule.setExpr(ruleExpComb);
        AlertRuleConfigDTO.Group.Rule.Labels labels = new AlertRuleConfigDTO.Group.Rule.Labels();
        labels.setLevel(alertTemplateRuleDO.getLevel());
        labels.setTemplateId(alertTemplateRuleDO.getTemplateId());
        labels.setTemplateRuleId(alertTemplateRuleDO.getId());
        rule.setLabels(labels);
        AlertRuleConfigDTO.Group.Rule.Annotations annotations = new AlertRuleConfigDTO.Group.Rule.Annotations();
        annotations.setSummary(alertTemplateRuleDO.getRuleName());
        annotations.setDescription(
            StrUtil.isBlank(alertTemplateRuleDO.getAlertDesc()) ? "" : alertTemplateRuleDO.getAlertDesc());
        rule.setAnnotations(annotations);
        List<AlertRuleConfigDTO.Group.Rule> rules = new ArrayList<>();
        rules.add(rule);
        group.setRules(rules);
        return group;
    }

    private String parseRuleExpComb(String ruleExpComb, List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS,
                                    String instances) {
        int position = 0;
        int start = 0;
        boolean isAnd = false;
        String ruleExp = "";
        while (ruleExpComb.indexOf(AND, position) > -1 || ruleExpComb.indexOf(OR, position) > -1) {
            String key = "";
            int andIdx = ruleExpComb.indexOf(AND, position);
            int orIdx = ruleExpComb.indexOf(OR, position);
            if (andIdx == -1) {
                position = orIdx;
                key = OR;
            } else if (ruleExpComb.indexOf(OR, position) == -1) {
                position = andIdx;
                key = AND;
            } else if (andIdx < orIdx) {
                position = andIdx;
                key = AND;
            } else {
                position = orIdx;
                key = OR;
            }
            String ruleMark = ruleExpComb.substring(start, position);
            AlertTemplateRuleItemDO alertTemplateRuleItemDO =
                alertTemplateRuleItemDOS.stream().filter(item -> item.getRuleMark().equals(ruleMark.trim()))
                    .findFirst().orElse(null);
            if (alertTemplateRuleItemDO == null) {
                continue;
            }
            ruleExp += subRuleExp(isAnd, alertTemplateRuleItemDO.getRuleExp(), alertTemplateRuleItemDO.getOperate(),
                alertTemplateRuleItemDO.getLimitValue()) + key;
            start = position + key.length();
            if (key.equals(AND)) {
                isAnd = true;
            } else {
                isAnd = false;
            }
            position++;
        }
        String ruleMark = ruleExpComb.substring(start);
        if (StrUtil.isNotBlank(ruleMark)) {
            AlertTemplateRuleItemDO alertTemplateRuleItemDO =
                alertTemplateRuleItemDOS.stream().filter(item -> item.getRuleMark().equals(ruleMark.trim()))
                    .findFirst().orElse(null);
            ruleExp += subRuleExp(isAnd, alertTemplateRuleItemDO.getRuleExp(), alertTemplateRuleItemDO.getOperate(),
                alertTemplateRuleItemDO.getLimitValue());
        }
        ruleExp = ruleExp.replaceAll("\\$\\{instances\\}", instances);
        return ruleExp;
    }

    private String subRuleExp(Boolean isAnd, String ruleExp, String operate, BigDecimal limitValue) {
        return (isAnd ? "on(instance) " : "") + ruleExp
            + (StrUtil.isNotBlank(operate) ? (operate + limitValue.toString()) : "");
    }

    /**
     * update prometheus rule configuration by templateId
     *
     * @param templateId Long
     */
    public synchronized void updateRuleConfigByTemplateId(Long templateId) {
        String clusterNodeIds = getClusterNodeIdsByTemplateId(templateId);
        if (StrUtil.isBlank(clusterNodeIds)) {
            return;
        }
        updateRuleConfig(templateId, clusterNodeIds);
    }

    private synchronized void updateRuleConfig(Long templateId, String clusterNodeIds) {
        // update the rule configuration file of the prometheus
        init();
        try {
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = alertTemplateRuleMapper.selectList(
                Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, templateId)
                    .eq(AlertTemplateRuleDO::getRuleType, CommonConstants.INDEX_RULE)
                    .eq(AlertTemplateRuleDO::getIsIncluded, CommonConstants.IS_INCLUDED)
                    .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
                    .eq(AlertTemplateRuleDO::getEnable, CommonConstants.ENABLE));
            if (CollectionUtil.isEmpty(alertTemplateRuleDOS)) {
                return;
            }
            Optional<AlertRuleConfigDTO> alertRuleConfigDtoOptional = getAlertRuleConfigDto(templateId);
            if (StrUtil.isBlank(clusterNodeIds) && alertRuleConfigDtoOptional.isEmpty()) {
                return;
            }
            AlertRuleConfigDTO config = alertRuleConfigDtoOptional.isEmpty() ? new AlertRuleConfigDTO()
                : alertRuleConfigDtoOptional.get();
            List<String> ruleNames = alertTemplateRuleDOS.stream().map(item -> "rule_" + item.getId()).collect(
                Collectors.toList());
            if (StrUtil.isBlank(clusterNodeIds)) {
                List<AlertRuleConfigDTO.Group> groups = config.getGroups().stream().filter(
                    item -> !ruleNames.contains(item.getName())).collect(Collectors.toList());
                if (!CollectionUtil.containsAll(groups, config.getGroups())) {
                    config.setGroups(groups);
                    uploadRuleConfig(config, templateId);
                }
                return;
            }
            if (CollectionUtil.isEmpty(config.getGroups())) {
                config.setGroups(new ArrayList<>());
            }
            String instances = String.join("|", clusterNodeIds.split(CommonConstants.DELIMITER));
            List<AlertRuleConfigDTO.Group> groupList = alertTemplateRuleDOS.stream().map(
                item -> createRuleGroup(item, instances)).collect(Collectors.toList());
            AlertRuleConfigDTO cloneConfig = ObjectUtil.clone(config);
            List<AlertRuleConfigDTO.Group> groups = cloneConfig.getGroups().stream().filter(
                item -> !ruleNames.contains(item.getName())).collect(Collectors.toList());
            groups.addAll(groupList);
            cloneConfig.setGroups(new ArrayList<>(new LinkedHashSet<>(groups)));
            if (config.equals(cloneConfig)) {
                return;
            }
            uploadRuleConfig(cloneConfig, templateId);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert rule fail");
        }
    }

    private String getClusterNodeIdsByTemplateId(Long templateId) {
        List<AlertClusterNodeConfDO> nodeConfList =
            clusterNodeConfMapper.selectList(Wrappers.<AlertClusterNodeConfDO>lambdaQuery()
                .eq(AlertClusterNodeConfDO::getTemplateId, templateId)
                .eq(AlertClusterNodeConfDO::getIsDeleted,
                    CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(nodeConfList)) {
            return "";
        }
        return nodeConfList.stream().map(item -> item.getClusterNodeId()).collect(
            Collectors.joining(CommonConstants.DELIMITER));
    }

    /**
     * update prometheus rule configuration by template rule
     *
     * @param alertTemplateRuleDO AlertTemplateRule
     */
    public synchronized void updateRuleByTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO) {
        Long templateId = alertTemplateRuleDO.getTemplateId();
        String clusterNodeIds = getClusterNodeIdsByTemplateId(templateId);
        if (StrUtil.isBlank(clusterNodeIds)) {
            return;
        }
        init();
        try {
            Optional<AlertRuleConfigDTO> configDtoOptional = getAlertRuleConfigDto(templateId);
            AlertRuleConfigDTO config = null;
            if (configDtoOptional.isEmpty()) {
                config = new AlertRuleConfigDTO();
            } else {
                config = configDtoOptional.get();
            }
            if (CollectionUtil.isEmpty(config.getGroups())) {
                config.setGroups(new ArrayList<>());
            }
            String ruleName = "rule_" + alertTemplateRuleDO.getId();
            String instances = String.join("|", clusterNodeIds.split(CommonConstants.DELIMITER));
            AlertRuleConfigDTO.Group ruleGroup = createRuleGroup(alertTemplateRuleDO, instances);
            AlertRuleConfigDTO cloneConfig = ObjectUtil.clone(config);
            List<AlertRuleConfigDTO.Group> groups = cloneConfig.getGroups().stream().filter(
                item -> !ruleName.equals(item.getName())).collect(Collectors.toList());
            groups.add(ruleGroup);
            cloneConfig.setGroups(new ArrayList<>(new LinkedHashSet<>(groups)));
            if (config.equals(cloneConfig)) {
                return;
            }
            uploadRuleConfig(cloneConfig, templateId);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert rule fail");
        }
    }

    /**
     * remove prometheus rule configuration by template rule
     *
     * @param alertTemplateRuleDO AlertTemplateRule
     */
    public synchronized void removeRuleByTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO) {
        Long templateId = alertTemplateRuleDO.getTemplateId();
        String clusterNodeIds = getClusterNodeIdsByTemplateId(templateId);
        if (StrUtil.isBlank(clusterNodeIds)) {
            return;
        }
        init();
        try {
            Optional<AlertRuleConfigDTO> configDtoOptional = getAlertRuleConfigDto(templateId);
            if (configDtoOptional.isEmpty() || CollectionUtil.isEmpty(configDtoOptional.get().getGroups())) {
                return;
            }
            AlertRuleConfigDTO config = configDtoOptional.get();
            List<AlertRuleConfigDTO.Group> groups = config.getGroups();
            List<AlertRuleConfigDTO.Group> newGroups = groups.stream().filter(
                item -> !item.getName().equals("rule_" + alertTemplateRuleDO.getId())).collect(
                Collectors.toList());
            config.setGroups(newGroups);
            uploadRuleConfig(config, templateId);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("remove the alert rule fail");
        }
    }
}