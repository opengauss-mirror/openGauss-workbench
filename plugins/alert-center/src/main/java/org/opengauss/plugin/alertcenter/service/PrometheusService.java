
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.plugin.alertcenter.config.properties.AlertProperty;
import org.opengauss.plugin.alertcenter.config.properties.AlertmanagerProperty;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.AlertRuleConfigDto;
import org.opengauss.plugin.alertcenter.dto.PrometheusConfigDto;
import org.opengauss.plugin.alertcenter.dto.PrometheusEnvDto;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;
import org.opengauss.plugin.alertcenter.entity.AlertConfig;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItem;
import org.opengauss.plugin.alertcenter.entity.NctigbaEnv;
import org.opengauss.plugin.alertcenter.mapper.AlertClusterNodeConfMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleMapper;
import org.opengauss.plugin.alertcenter.mapper.NctigbaEnvMapper;
import org.opengauss.plugin.alertcenter.utils.SshSession;
import org.opengauss.plugin.alertcenter.utils.YamlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/19 09:50
 * @description 用于请求prometheus数据或者修改其配置
 */
@Service
@Slf4j
public class PrometheusService {
    @Autowired
    private AlertTemplateRuleMapper alertTemplateRuleMapper;

    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;

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

    @Autowired
    private AlertClusterNodeConfMapper clusterNodeConfMapper;

    private PrometheusEnvDto prometheusEnvDto;

    private ObjectMapper objectMapper = new ObjectMapper();

    private void initPrometheusEnvDto() {
        List<AlertConfig> alertConfigs = alertConfigService.list();
        if (prometheusEnvDto == null) {
            prometheusEnvDto = new PrometheusEnvDto();
        }
        prometheusEnvDto.setConfigList(alertConfigs);
        NctigbaEnv promEnv = envMapper.selectOne(
            Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.Type.PROMETHEUS));
        if (promEnv == null) {
            throw new ServiceException("uninstall the prometheus");
        }
        if (StrUtil.isBlank(promEnv.getPath())) {
            throw new ServiceException("uninstall the prometheus");
        }
        if (StrUtil.isBlank(promEnv.getHostid())) {
            throw new ServiceException("host id is unknown");
        }
        OpsHostEntity promeHost = hostFacade.getById(promEnv.getHostid());
        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(promEnv.getHostid());
        if (CollectionUtil.isEmpty(hostUserList)) {
            throw new ServiceException("host user is null");
        }
        List<OpsHostUserEntity> promUserList = hostUserList.stream().filter(
            item -> item.getUsername().equals(promEnv.getUsername())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(promUserList)) {
            throw new ServiceException("the host user Prometheus is not exist");
        }
        OpsHostUserEntity promUser = promUserList.get(0);
        // The code comments need to be uncommented in the development environment:  encryptionUtils.refreshKeyPair()
        prometheusEnvDto.setPromIp(promeHost.getPublicIp()).setHostPort(promeHost.getPort()).setPromPort(
            promEnv.getPort()).setPromUsername(promUser.getUsername()).setPromPasswd(
            encryptionUtils.decrypt(promUser.getPassword())).setPath(promEnv.getPath());
    }

    private PrometheusConfigDto getPromConfig() {
        //        String promYmlStr = session.execute("cat " + promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
        String promYmlStr = "";
        String url = "http://" + prometheusEnvDto.getPromIp() + ":" + prometheusEnvDto.getPromPort() + "/api/v1/status"
            + "/config";
        String result = HttpUtil.get(url);
        if (StrUtil.isNotBlank(result)) {
            JSONObject resultJson = new JSONObject(result);
            String status = resultJson.getStr("status", "");
            if (status.equals("success")) {
                JSONObject data = resultJson.getJSONObject("data");
                promYmlStr = data.getStr("yaml");
            }
        }
        HashMap map = YamlUtil.loadAs(promYmlStr, HashMap.class);
        if (map == null) {
            throw new NullPointerException("the prometheus config is null");
        }
        return objectMapper.convertValue(map, PrometheusConfigDto.class);
    }

    private void updatePromConfig(SshSession session, PrometheusConfigDto config) throws IOException {
        List<String> ruleFilesConf = config.getRuleFiles();
        String ruleFilePrefix = alertProperty.getRuleFilePrefix();
        String ruleFileSuffix = alertProperty.getRuleFileSuffix();
        String ruleFilePath =
            prometheusEnvDto.getPath() + CommonConstants.SLASH + ruleFilePrefix + "*" + ruleFileSuffix;
        boolean isConfigChange = false;
        if (CollectionUtils.isEmpty(ruleFilesConf)) {
            ruleFilesConf = new ArrayList<>();
            config.setRuleFiles(ruleFilesConf);
            ruleFilesConf.add(ruleFilePath);
            isConfigChange = true;
        }
        if (!ruleFilesConf.contains(ruleFilePath)) {
            ruleFilesConf.add(ruleFilePath);
            isConfigChange = true;
        }
        isConfigChange |= checkAndUpdatePromAlertConfig(config);

        if (isConfigChange) {
            uploadFileConfig(session, config);
        }
    }

    private boolean checkAndUpdatePromAlertConfig(PrometheusConfigDto config) {
        PrometheusConfigDto.Alert alerting = config.getAlerting();
        if (alerting == null) {
            alerting = new PrometheusConfigDto.Alert();
        }
        AlertmanagerProperty alertmanagerPro = alertProperty.getAlertmanager();
        String apiVersion = alertmanagerPro.getApiVersion();
        String pathPrefix = alertmanagerPro.getPathPrefix();
        List<PrometheusConfigDto.Alert.Alertmanager.Conf> staticConfigs = new ArrayList<>();
        PrometheusConfigDto.Alert.Alertmanager.Conf confNew = new PrometheusConfigDto.Alert.Alertmanager.Conf();
        List<String> targets = prometheusEnvDto.getConfigList().stream().map(
            item -> item.getAlertIp() + ":" + item.getAlertPort()).collect(Collectors.toList());
        confNew.setTargets(targets);
        staticConfigs.add(confNew);
        PrometheusConfigDto.Alert.Alertmanager alertmanagerNew = new PrometheusConfigDto.Alert.Alertmanager();
        alertmanagerNew.setPathPrefix(pathPrefix);
        alertmanagerNew.setApiVersion(apiVersion);
        alertmanagerNew.setStaticConfigs(staticConfigs);
        alertmanagerNew.setScheme("http");
        alertmanagerNew.setIsFollowRedirects(true);
        alertmanagerNew.setTimeout("10s");
        alertmanagerNew.setIsEnableHttp2(true);
        List<PrometheusConfigDto.Alert.Alertmanager> alertmanagers = alerting.getAlertmanagers();
        if (CollectionUtils.isEmpty(alertmanagers)) {
            alertmanagers = new ArrayList<PrometheusConfigDto.Alert.Alertmanager>();
            alertmanagers.add(alertmanagerNew);
            return true;
        } else {
            PrometheusConfigDto.Alert.Alertmanager alertmanager = alertmanagers.get(0);
            if (!alertmanager.toString().equals(alertmanagerNew.toString())) {
                List<PrometheusConfigDto.Alert.Alertmanager.Conf> staticConfigs0 = alertmanager.getStaticConfigs();
                if (CollectionUtil.isNotEmpty(staticConfigs0)) {
                    List<String> targets0 = staticConfigs0.get(0).getTargets();
                    List<String> delTargets = CollectionUtil.isNotEmpty(
                        prometheusEnvDto.getDelConfigList()) ? prometheusEnvDto.getDelConfigList().stream().map(
                        item -> item.getAlertIp() + ":" + item.getAlertPort()).collect(
                        Collectors.toList()) : new ArrayList<>();
                    targets0 = targets0.stream().filter(
                        item -> !delTargets.contains(item) || !targets.contains(item)).collect(Collectors.toList());
                    targets.addAll(targets0);
                }
                alertmanagers.set(0, alertmanagerNew);
                return true;
            }
        }
        return false;
    }

    private void uploadFileConfig(SshSession session, PrometheusConfigDto config) throws IOException {
        HashMap configMap = objectMapper.convertValue(config, HashMap.class);
        File prometheusConfigFile = File.createTempFile("prom", ".tmp");
        FileUtil.appendUtf8String(YamlUtil.dump(configMap), prometheusConfigFile);
        String path = prometheusEnvDto.getPath() + (prometheusEnvDto.getPath().endsWith(CommonConstants.SLASH) ? "" :
            CommonConstants.SLASH) + CommonConstants.PROMETHEUS_YML;
        session.execute("rm " + path);
        session.upload(prometheusConfigFile.getCanonicalPath(), path);
        Files.delete(prometheusConfigFile.toPath());
        String res = HttpUtil.post(
            "http://" + prometheusEnvDto.getPromIp() + ":" + +prometheusEnvDto.getPromPort() + "/-/reload", "");
        log.info(res);
    }

    public void initPrometheusConfig() {
        try {
            initPrometheusEnvDto();
            List<AlertConfig> alertConfigs = prometheusEnvDto.getConfigList();
            if (CollectionUtil.isEmpty(alertConfigs)) {
                return;
            }
            SshSession session = SshSession.connect(prometheusEnvDto.getPromIp(), prometheusEnvDto.getHostPort(),
                prometheusEnvDto.getPromUsername(), prometheusEnvDto.getPromPasswd());
            PrometheusConfigDto config = getPromConfig();
            updatePromConfig(session, config);
            session.close();
        } catch (IOException | CryptoException e) {
            log.error("init prometheus configuration fail: ", e);
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
     *              默认1个小时,this.props.options.range=60 * 60 * 1000
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
    public Number[][] queryRange(
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
            return new Number[0][0];
        }
        JSONObject resJson = new JSONObject(res);
        if (!resJson.getStr("status", "").equals("success")) {
            return new Number[0][0];
        }
        JSONObject data = resJson.getJSONObject("data");
        if (data == null) {
            return new Number[0][0];
        }
        JSONArray result = data.getJSONArray("result");
        if (CollectionUtil.isEmpty(result)) {
            return new Number[0][0];
        }
        JSONObject metricInfo = result.getJSONObject(0);
        if (metricInfo == null) {
            return new Number[0][0];
        }
        JSONArray values = metricInfo.getJSONArray("values");
        Number[][] dataResult = new Number[values.size()][2];
        for (int i = 0; i < values.size(); i++) {
            JSONArray valArr = values.getJSONArray(i);
            dataResult[i][0] = valArr.getLong(0) * 1000;
            dataResult[i][1] = valArr.getBigDecimal(1).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return dataResult;
    }

    public void updateRuleConfig(Collection<Long> templateIdList, Collection<String> delNodeConfIds) {
        if (CollectionUtil.isEmpty(templateIdList)) {
            return;
        }
        initPrometheusEnvDto();
        try {
            SshSession session = SshSession.connect(prometheusEnvDto.getPromIp(), prometheusEnvDto.getHostPort(),
                prometheusEnvDto.getPromUsername(), prometheusEnvDto.getPromPasswd());
            for (Long templateId : templateIdList) {
                updateRuleConfig(session, templateId, delNodeConfIds);
            }
            session.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert rule fail");
        }
    }


    public void updateRuleConfig(Long templateId, Collection<String> delNodeConfIds) {
        if (templateId == null) {
            return;
        }
        // 更新prometheus规则配置文件
        try {
            initPrometheusEnvDto();
            SshSession session = SshSession.connect(prometheusEnvDto.getPromIp(), prometheusEnvDto.getHostPort(),
                prometheusEnvDto.getPromUsername(), prometheusEnvDto.getPromPasswd());
            updateRuleConfig(session, templateId, delNodeConfIds);
            session.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert rule fail");
        }
    }

    private void updateRuleConfig(
        SshSession session, Long templateId, Collection<String> delNodeConfIds) throws IOException {
        List<AlertClusterNodeConf> alertClusterNodeConfs = clusterNodeConfMapper.selectList(
            Wrappers.<AlertClusterNodeConf>lambdaQuery().eq(AlertClusterNodeConf::getTemplateId, templateId).eq(
                    AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE)
                .notIn(CollectionUtil.isNotEmpty(delNodeConfIds),
                    AlertClusterNodeConf::getClusterNodeId, delNodeConfIds));

        AlertRuleConfigDto config = getAlertRuleConfigDto(session, templateId, alertClusterNodeConfs);
        if (config == null || config.getGroups() == null) {
            return;
        }
        String instances = alertClusterNodeConfs.stream().map(item -> item.getClusterNodeId()).collect(
            Collectors.joining("|"));
        List<AlertTemplateRule> alertTemplateRules = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId));
        if (checkAndUpdateRuleConfig(instances, config, alertTemplateRules)) {
            uploadRuleFileConfig(session, config, templateId);
        }
    }

    private void uploadRuleFileConfig(
        SshSession session, AlertRuleConfigDto config, Long templateId) throws IOException {
        HashMap configMap = objectMapper.convertValue(config, HashMap.class);
        File ruleConfigFile = File.createTempFile("rule", ".tmp");
        FileUtil.appendUtf8String(YamlUtil.dump(configMap), ruleConfigFile);
        String dir = prometheusEnvDto.getPath() + CommonConstants.SLASH + alertProperty.getRuleFilePrefix();
        String fileName = "rule_template_" + templateId + alertProperty.getRuleFileSuffix();
        String path = dir + fileName;
        session.execute("rm " + path);
        session.upload(ruleConfigFile.getCanonicalPath(), path);
        Files.delete(ruleConfigFile.toPath());
        String res = HttpUtil.post(
            "http://" + prometheusEnvDto.getPromIp() + ":" + +prometheusEnvDto.getPromPort() + "/-/reload", "");
        log.info(res);
    }

    private AlertRuleConfigDto getAlertRuleConfigDto(
        SshSession session, Long templateId,
        List<AlertClusterNodeConf> alertClusterNodeConfs) throws IOException {
        String ruleDir = alertProperty.getRuleFilePrefix().endsWith(CommonConstants.SLASH)
            ? alertProperty.getRuleFilePrefix().substring(0,
            alertProperty.getRuleFilePrefix().length() - 1) : alertProperty.getRuleFilePrefix();
        String dirFileStr = session.execute("ls " + prometheusEnvDto.getPath());
        String[] dirFileArr = dirFileStr.split(CommonConstants.LINE_SEPARATOR);
        String dir = prometheusEnvDto.getPath() + CommonConstants.SLASH + alertProperty.getRuleFilePrefix();
        AlertRuleConfigDto config = new AlertRuleConfigDto();
        if (!Arrays.asList(dirFileArr).contains(ruleDir)) {
            if (CollectionUtil.isEmpty(alertClusterNodeConfs)) {
                return config;
            }
            session.execute("mkdir " + dir);
        }
        String ruleFileStr = session.execute("ls " + dir);
        String fileName = "rule_template_" + templateId + alertProperty.getRuleFileSuffix();
        String path = dir + fileName;
        if (StrUtil.isBlank(ruleFileStr)) {
            if (CollectionUtil.isEmpty(alertClusterNodeConfs)) {
                return config;
            }
            session.execute("touch " + path);
        } else {
            String[] ruleFileArr = ruleFileStr.split(CommonConstants.LINE_SEPARATOR);
            if (Arrays.asList(ruleFileArr).contains(fileName)) {
                if (CollectionUtil.isEmpty(alertClusterNodeConfs)) {
                    // delete rule_template_xxx.yml
                    session.execute("rm -f " + path);
                    HttpUtil.post("http://" + prometheusEnvDto.getPromIp() + ":" + prometheusEnvDto.getPromPort()
                        + "/-/reload", "");
                    return config;
                }
            } else {
                session.execute("touch " + path);
            }
        }

        String ruleYmlStr = session.execute("cat " + path);
        Map map = YamlUtil.loadAs(ruleYmlStr, HashMap.class);
        if (map != null) {
            config = objectMapper.convertValue(map, AlertRuleConfigDto.class);
        } else {
            config.setGroups(new ArrayList<>());
        }
        return config;
    }

    private boolean checkAndUpdateRuleConfig(
        String instances, AlertRuleConfigDto config,
        List<AlertTemplateRule> alertTemplateRules) {
        List<AlertRuleConfigDto.Group> groups = config.getGroups();
        if (groups == null) {
            groups = new ArrayList<>();
            config.setGroups(groups);
        }
        boolean isConfigChange = false;
        for (AlertTemplateRule alertTemplateRule : alertTemplateRules) {
            AlertRuleConfigDto.Group groupNew = templateRuleToPromRuleGroup(alertTemplateRule, instances);
            if (groups.size() == 0) {
                groups.add(groupNew);
                isConfigChange = true;
                continue;
            }
            List<String> groupNames = groups.stream().map(item -> item.getName()).collect(Collectors.toList());
            if (!groupNames.contains(groupNew.getName())) {
                groups.add(groupNew);
                isConfigChange = true;
                continue;
            }
            for (int i = 0; i < groups.size(); i++) {
                AlertRuleConfigDto.Group group = groups.get(i);
                if (group.getName().equals(groupNew.getName()) && !group.toString().equals(groupNew.toString())) {
                    groups.set(i, groupNew);
                    isConfigChange = true;
                    break;
                }
            }
        }
        return isConfigChange;
    }

    private AlertRuleConfigDto.Group templateRuleToPromRuleGroup(
        AlertTemplateRule alertTemplateRule,
        String instances) {
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                alertTemplateRule.getId()));
        String ruleExpComb = alertTemplateRule.getRuleExpComb();
        for (AlertTemplateRuleItem alertTemplateRuleItem : alertTemplateRuleItems) {
            ruleExpComb = ruleExpComb.replace(alertTemplateRuleItem.getRuleMark(),
                alertTemplateRuleItem.getRuleExp() + " " + alertTemplateRuleItem.getOperate()
                    + alertTemplateRuleItem.getLimitValue());
        }
        ruleExpComb = ruleExpComb.replaceAll("\\$\\{instances\\}", instances);
        AlertRuleConfigDto.Group groupNew = new AlertRuleConfigDto.Group();
        String name = "rule_" + alertTemplateRule.getId();
        groupNew.setName(name);
        AlertRuleConfigDto.Group.Rule rule = new AlertRuleConfigDto.Group.Rule();
        rule.setAliasFor(alertTemplateRule.getNotifyDuration() + alertTemplateRule.getNotifyDurationUnit());
        rule.setAlert(name);
        rule.setExpr(ruleExpComb);
        AlertRuleConfigDto.Group.Rule.Labels labels = new AlertRuleConfigDto.Group.Rule.Labels();
        labels.setLevel(alertTemplateRule.getLevel());
        labels.setTemplateId(alertTemplateRule.getTemplateId());
        labels.setTemplateRuleId(alertTemplateRule.getId());
        rule.setLabels(labels);
        AlertRuleConfigDto.Group.Rule.Annotations annotations = new AlertRuleConfigDto.Group.Rule.Annotations();
        annotations.setSummary(alertTemplateRule.getRuleName());
        annotations.setDescription(
            StrUtil.isBlank(alertTemplateRule.getAlertDesc()) ? "" : alertTemplateRule.getAlertDesc());
        rule.setAnnotations(annotations);
        List<AlertRuleConfigDto.Group.Rule> rules = new ArrayList<>();
        rules.add(rule);
        groupNew.setRules(rules);
        return groupNew;
    }

    public void updateAlertConfig(AlertConfig alertConfig, List<AlertConfig> delAlertConfigList) {
        try {
            if (StrUtil.isBlank(alertConfig.getAlertIp()) && StrUtil.isBlank(alertConfig.getAlertPort())) {
                throw new ServiceException("The alert IP or the alert Port is not empty!");
            }
            initPrometheusEnvDto();
            List<AlertConfig> configList = new ArrayList<>();
            configList.add(alertConfig);
            prometheusEnvDto.setConfigList(configList).setDelConfigList(delAlertConfigList);

            SshSession session = SshSession.connect(prometheusEnvDto.getPromIp(), prometheusEnvDto.getHostPort(),
                prometheusEnvDto.getPromUsername(), prometheusEnvDto.getPromPasswd());
            PrometheusConfigDto config = getPromConfig();
            updatePromConfig(session, config);
            session.close();
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("update the alert config fail");
        }
    }
}
