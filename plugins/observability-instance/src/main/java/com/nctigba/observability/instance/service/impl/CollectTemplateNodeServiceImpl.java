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
 *  CollectTemplateNodeServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/CollectTemplateNodeServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.mapper.PromAgentRelationMapper;
import com.nctigba.observability.instance.model.dto.PrometheusConfigNodeDTO;
import com.nctigba.observability.instance.model.dto.PrometheusConfigNodeDetailDTO;
import com.nctigba.observability.instance.model.dto.SetNodeTemplateDTO;
import com.nctigba.observability.instance.model.dto.SetNodeTemplateDirectDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateMetricsDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateNodeDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.exception.TipsException;
import com.nctigba.observability.instance.mapper.CollectTemplateMapper;
import com.nctigba.observability.instance.mapper.CollectTemplateMetricsMapper;
import com.nctigba.observability.instance.mapper.CollectTemplateNodeMapper;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.entity.PromAgentRelationDO;
import com.nctigba.observability.instance.service.AgentNodeRelationService;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.CollectTemplateNodeService;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.PrometheusService;
import com.nctigba.observability.instance.util.SshSessionUtils;
import com.nctigba.observability.instance.util.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @since 2023-11-02
 */
@Service
@Slf4j
public class CollectTemplateNodeServiceImpl
        extends ServiceImpl<CollectTemplateNodeMapper, CollectTemplateNodeDO>
        implements CollectTemplateNodeService {
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    @Autowired
    HostFacade hostFacade;
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    @Autowired
    HostUserFacade hostUserFacade;
    @Autowired
    MetricsService metricsService;
    @Autowired
    CollectTemplateMapper collectTemplateMapper;
    @Autowired
    CollectTemplateNodeMapper collectTemplateNodeMapper;
    @Autowired
    CollectTemplateMetricsMapper collectTemplateMetricsMapper;
    @Autowired
    NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    EncryptionUtils encryptionUtils;
    @Autowired
    ClusterManager clusterManager;
    @Autowired
    AgentNodeRelationService agentNodeRelationService;
    @Autowired
    private PromAgentRelationMapper promAgentRelationMapper;

    @Override
    @Transactional
    @DS("embedded")
    public Integer setNodeTemplateDirect(SetNodeTemplateDirectDTO setNodeTemplateDirectDTO) {
        long startTime = System.currentTimeMillis();
        Integer templateId = deleteNodeOldTemplateAndBuildNewOne(setNodeTemplateDirectDTO);

        // call setNodeTemplate
        SetNodeTemplateDTO setNodeTemplateDTO = new SetNodeTemplateDTO();
        setNodeTemplateDTO.setNodeId(setNodeTemplateDirectDTO.getNodeId());
        setNodeTemplateDTO.setTemplateId(templateId);
        setNodeTemplate(setNodeTemplateDTO);
        return templateId;
    }

    private Integer deleteNodeOldTemplateAndBuildNewOne(SetNodeTemplateDirectDTO setNodeTemplateDirectDTO) {
        // get node template id
        List<Integer> templateIds = collectTemplateNodeMapper.selectList(
                new LambdaQueryWrapper<CollectTemplateNodeDO>()
                    .eq(CollectTemplateNodeDO::getNodeId, setNodeTemplateDirectDTO.getNodeId()))
            .stream()
            .map(z -> z.getTemplateId())
            .collect(Collectors.toList());

        // if has template, delete template and template detail
        if (!templateIds.isEmpty()) {
            collectTemplateMapper.delete(
                new LambdaQueryWrapper<CollectTemplateDO>()
                    .in(CollectTemplateDO::getId, templateIds));
            collectTemplateNodeMapper.delete(
                new LambdaQueryWrapper<CollectTemplateNodeDO>()
                    .eq(CollectTemplateNodeDO::getNodeId, setNodeTemplateDirectDTO.getNodeId()));
            collectTemplateMetricsMapper.delete(
                new LambdaQueryWrapper<CollectTemplateMetricsDO>()
                    .in(CollectTemplateMetricsDO::getTemplateId, templateIds));
        }

        // build new template
        CollectTemplateDO newTemplate = new CollectTemplateDO();
        newTemplate.setName("Template for Node " + setNodeTemplateDirectDTO.getNodeId());
        collectTemplateMapper.insert(newTemplate);
        List<CollectTemplateMetricsDO> toInsertMetrics = new ArrayList<>();
        setNodeTemplateDirectDTO.getDetails().forEach(z -> {
            if (StrUtil.isBlank(z.getInterval())) {
                return;
            }
            CollectTemplateMetricsDO newTemplateMetrics = new CollectTemplateMetricsDO();
            newTemplateMetrics.setTemplateId(newTemplate.getId());
            newTemplateMetrics.setMetricsKey(z.getMetricKey());
            newTemplateMetrics.setInterval(z.getInterval());
            newTemplateMetrics.setIsEnable(z.getIsEnable());
            toInsertMetrics.add(newTemplateMetrics);
        });
        if(!toInsertMetrics.isEmpty()){
            collectTemplateMetricsMapper.insertBatchTemplateMetrics(toInsertMetrics);
        }
        return newTemplate.getId();
    }

    @Override
    @Transactional
    @DS("embedded")
    public void setNodeTemplate(SetNodeTemplateDTO templateNodeDTO) {
        String nodeId = templateNodeDTO.getNodeId();

        // delete node template data
        collectTemplateNodeMapper.delete(
                new LambdaQueryWrapper<CollectTemplateNodeDO>()
                        .eq(CollectTemplateNodeDO::getNodeId, nodeId)
        );

        // save node template data
        List<CollectTemplateNodeDO> entities = new ArrayList<>();
        CollectTemplateNodeDO entity = new CollectTemplateNodeDO();
        entity.setTemplateId(templateNodeDTO.getTemplateId());
        entity.setNodeId(nodeId);
        entities.add(entity);
        collectTemplateNodeMapper.insert(entity);
    }

    @Override
    public List<PrometheusConfigNodeDTO> getNodePrometheusConfigParam(Integer templateId, List<String> nodeIds) {
        List<PrometheusConfigNodeDTO> configNodes = new ArrayList<>();
        nodeIds.forEach(nodeId -> {
            // build scrape_configs with node config
            // because job name 1:1 scrape_interval,so 1 node 1scrape,make 1 scrape_config
            // if no config,then 1 scrape_configs with no config
            // get template configs
            List<CollectTemplateMetricsDO> templateMetrics =
                    collectTemplateMetricsMapper.selectList(
                            new LambdaQueryWrapper<CollectTemplateMetricsDO>()
                                    .eq(CollectTemplateMetricsDO::getTemplateId, templateId)
                    );

            // get all metrics
            List<String> metricGroupKeys = new ArrayList<>();
            try {
                metricGroupKeys = metricsService.getAllMetricKeys();
            } catch (IOException e) {
                throw new TipsException("getMetricGroupKeyError");
            }

            // metrics not in template configs, add into default scrape time
            List<CollectTemplateMetricsDO> defaultScrapeConfig = new ArrayList<>();
            metricGroupKeys.forEach(z -> {
                if (!templateMetrics.stream().anyMatch(t -> t.getMetricsKey().equals(z))) {
                    CollectTemplateMetricsDO entity = new CollectTemplateMetricsDO();
                    entity.setMetricsKey(z);
                    entity.setInterval(CommonConstants.DEFAULT_SCRAPE_TIME);
                    entity.setIsEnable(true);
                    defaultScrapeConfig.add(entity);
                }
            });
            // template configs group by interval
            Map<String, List<CollectTemplateMetricsDO>> groupedByInterval = templateMetrics.stream()
                    .collect(Collectors.groupingBy(CollectTemplateMetricsDO::getInterval));
            if (!defaultScrapeConfig.isEmpty()) {
                if (!groupedByInterval.containsKey(CommonConstants.DEFAULT_SCRAPE_TIME)) {
                    groupedByInterval.put(CommonConstants.DEFAULT_SCRAPE_TIME, defaultScrapeConfig);
                } else {
                    groupedByInterval.get(CommonConstants.DEFAULT_SCRAPE_TIME).addAll(defaultScrapeConfig);
                }
            }

            List<PrometheusConfigNodeDetailDTO> configNodeDetails = new ArrayList<>();
            groupedByInterval.forEach((scrapInterval, entities) -> {
                List<String> metricNames = entities.stream()
                    .filter(CollectTemplateMetricsDO::getIsEnable)
                        .map(CollectTemplateMetricsDO::getMetricsKey)
                        .collect(Collectors.toList());
                PrometheusConfigNodeDetailDTO detail = new PrometheusConfigNodeDetailDTO(metricNames, scrapInterval);
                configNodeDetails.add(detail);
            });

            PrometheusConfigNodeDTO configNode = new PrometheusConfigNodeDTO(nodeId);
            configNode.setDetails(configNodeDetails);
            configNodes.add(configNode);
        });
        return configNodes;
    }

    /**
     * To update prometheus configs
     *
     * @param envPromId String
     * @param configNodes List<PrometheusConfigNodeDTO>
     * @param delNodeIds List<String>
     */
    @DS("private")
    @Override
    public void setPrometheusConfig(String envPromId, List<PrometheusConfigNodeDTO> configNodes,
                                    List<String> delNodeIds) {
        // is Prometheus installed and normal operation
        var promEnv = envMapper.selectById(envPromId);
        if (promEnv == null) {
            throw new TipsException("prometheus not exists");
        }
        if (StrUtil.isBlank(promEnv.getPath())) {
            throw new TipsException("prometheus not exists");
        }
        // get prometheus host and user
        var promeHost = hostFacade.getById(promEnv.getHostid());
        var promUser = hostUserFacade.listHostUserByHostId(promEnv.getHostid()).stream()
            .filter(p -> p.getUsername().equals(promEnv.getUsername())).findFirst()
            .orElseThrow(() -> new RuntimeException(
                "The node information corresponding to the host is not found"));

        try (var promSession = SshSessionUtils.connect(promeHost.getPublicIp(), promeHost.getPort(),
            promEnv.getUsername(), encryptionUtils.decrypt(promUser.getPassword()))) {
            // download Prometheus config file
            var promYmlStr = promSession
                .execute("cat " + promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
            var conf = YamlUtils.loadAs(promYmlStr, PrometheusService.prometheusConfig.class);

            if (CollectionUtil.isNotEmpty(delNodeIds)) {
                for (String delNodeId : delNodeIds) {
                    conf.getScrape_configs().removeIf(
                        oldConfigs -> oldConfigs.getJob_name().contains(delNodeId)
                    );
                }
            }
            // loop related nodes
            for (PrometheusConfigNodeDTO z : configNodes) {
                String nodeId = z.getNodeId();
                List<AgentNodeRelationDO> agentNodeRelList = agentNodeRelationService.list(
                    new LambdaQueryWrapper<AgentNodeRelationDO>()
                        .eq(AgentNodeRelationDO::getNodeId, nodeId));
                if (CollectionUtil.isEmpty(agentNodeRelList)) {
                    throw new TipsException("Agent node relation not found for node :" + nodeId);
                }

                // get node info
                ClusterManager.OpsClusterNodeVOSub node = clusterManager.getOpsNodeById(nodeId);
                if (node == null) {
                    throw new TipsException("node not found");
                }
                // remove old configs
                conf.getScrape_configs().removeIf(
                    oldConfigs -> oldConfigs.getJob_name().contains(nodeId)
                );
                List<String> targets = new ArrayList<>();
                for (AgentNodeRelationDO agentNodeRelationDO : agentNodeRelList) {
                    NctigbaEnvDO evnNode = envMapper.selectOne(
                        Wrappers.<NctigbaEnvDO>lambdaQuery()
                            .eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.EXPORTER.name())
                            .eq(NctigbaEnvDO::getId, agentNodeRelationDO.getEnvId()));
                    if (evnNode == null) {
                        agentNodeRelationService.remove(Wrappers.<AgentNodeRelationDO>lambdaQuery()
                            .eq(AgentNodeRelationDO::getNodeId, nodeId)
                            .eq(AgentNodeRelationDO::getEnvId, agentNodeRelationDO.getEnvId()));
                        continue;
                    }
                    // get host data
                    OpsHostEntity hostEntity = hostFacade.getById(evnNode.getHostid());
                    if (hostEntity == null) {
                        throw new TipsException(CommonConstants.HOST_NOT_FOUND);
                    }
                    // new staticConfigs
                    String agentPort = evnNode.getPort().toString();
                    targets.add(hostEntity.getPublicIp() + ":" + agentPort);
                }
                if (CollectionUtil.isEmpty(targets)) {
                    throw new TipsException("Agent node relation not found for node :" + nodeId);
                }

                var staticConfigs = new PrometheusService.prometheusConfig.job.conf();
                staticConfigs.setLabels(Map.of("instance", nodeId, "type", "exporter"));
                staticConfigs.setTargets(targets);

                // loop same node id by diff scrape interval
                z.getDetails().forEach(detail -> {
                    // new job
                    var job = new PrometheusService.prometheusConfig.job();
                    job.setStatic_configs(Arrays.asList(staticConfigs));
                    job.setJob_name(nodeId + CommonConstants.UNDERLINE + detail.getScrapeInterval());
                    job.setScrape_interval(detail.getScrapeInterval());

                    // set params
                    job.setParams(Map.of("name[]", detail.getMetricNames().toArray(new String[0]),
                        "nodeId", new String[]{z.getNodeId()}));

                    conf.getScrape_configs().add(job);
                });
            }
            // write new prometheus.yml to temp file
            log.debug("conf:{}", conf);
            var prometheusConfigFile = File.createTempFile("prom", ".tmp");
            FileUtil.appendUtf8String(YamlUtils.dump(conf), prometheusConfigFile);

            // upload
            promSession.upload(prometheusConfigFile.getCanonicalPath(),
                promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
            Files.delete(prometheusConfigFile.toPath());
        } catch (IOException e) {
            throw new TipsException(e);
        }

        // refresh Prometheus config
        // curl -X POST http://IP/-/reload
        String url = "http://" + promeHost.getPublicIp() + ":" + promEnv.getPort() + "/-/reload";
        String res = HttpUtil.post(url, "");
        log.debug("refresh prometheus url:{}", url);
        log.debug("refresh prometheus result:{}", StrUtil.isBlank(res) ? "Succeed!" : res);
        if ("Lifecycle API is not enabled.".equals(res)) {
            throw new TipsException("Lifecycle API is not enabled.");
        }
    }

    @Override
    public void setNodePrometheusConfig(String nodeId, Integer templateId) {
        // setPrometheusConfig
        List<AgentNodeRelationDO> agentNodeRelList = agentNodeRelationService.list(
            Wrappers.<AgentNodeRelationDO>lambdaQuery().eq(AgentNodeRelationDO::getNodeId, nodeId));
        if (CollectionUtil.isEmpty(agentNodeRelList)) {
            throw new CustomException("agent-node-relation not found!");
        }
        String envAgentId = agentNodeRelList.get(0).getEnvId();
        List<PromAgentRelationDO> promAgentRelList = promAgentRelationMapper.selectList(
            Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvAgentId, envAgentId));
        if (CollectionUtil.isEmpty(promAgentRelList)) {
            throw new CustomException("prometheus-agent-relation not found!");
        }
        String envPromId = promAgentRelList.get(0).getEnvPromId();
        List<PrometheusConfigNodeDTO> configNodes = getNodePrometheusConfigParam(
            templateId, Arrays.asList(nodeId));
        setPrometheusConfig(envPromId, configNodes, null);
    }
}
