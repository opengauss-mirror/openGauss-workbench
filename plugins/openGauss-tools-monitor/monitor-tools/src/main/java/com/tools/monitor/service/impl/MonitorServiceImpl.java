package com.tools.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tools.monitor.common.contant.Constants;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.zabbix.SourceName;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.mapper.SysJobMapper;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitorServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private SysSourceTargetMapper sourceTargetMapper;

    @Autowired
    private SysJobMapper sysJobMapper;

    @Autowired
    private SysJobServiceImpl sysJobService;


    private static final String CONNAME = "^[A-Za-z0-9_]{2,16}$";

    @Override
    public ResponseVO setSourceConfig(SysConfig sysConfig) {
        if (ObjectUtil.isEmpty(sysConfig)) {
            return ResponseVO.errorResponseVO("配置信息为空");
        }
        String name = sysConfig.getConnectName();
        if (!sysConfig.getPlatform().equals(Constants.NAGIOS)) {
            if (ObjectUtil.isEmpty(name) || !name.matches(CONNAME)) {
                return ResponseVO.errorResponseVO("连接名称只支持英文、下划线、数字组合2-16个字符");
            }
        }
        sysConfig.setIsCreate(true);
        String message = configMapper.saveConfig(sysConfig);
        if (StrUtil.isNotBlank(message)) {
            return ResponseVO.errorResponseVO(message);
        }
        return ResponseVO.successResponseVO("数据源配置成功");
    }

    @Override
    public ResponseVO updateConfig(SysConfig sysConfig) {
        if (ObjectUtil.isEmpty(sysConfig)) {
            return ResponseVO.errorResponseVO("配置信息为空");
        }
        if (ObjectUtil.isNotEmpty(sysConfig) && StrUtil.isNotBlank(sysConfig.getPlatform())) {
            if (Constants.ZABBIX.equals(sysConfig.getPlatform()) || Constants.NAGIOS.equals(sysConfig.getPlatform())) {
                sysConfig.setIsCreate(false);
                String message = configMapper.saveConfig(sysConfig);
                if (StrUtil.isNotBlank(message)) {
                    return ResponseVO.errorResponseVO(message);
                }
            }
            if (Constants.PROM.equals(sysConfig.getPlatform())) {
                sysConfig.setIsCreate(false);
                String str = configMapper.updateProm(sysConfig);
                if (StrUtil.isNotBlank(str)) {
                    return ResponseVO.errorResponseVO(str);
                }
            }
        }
        return ResponseVO.successResponseVO("修改成功");
    }

    @Override
    public ResponseVO getDataSourceList(Integer page, Integer size) {
        List<SysConfig> sysConfigList = configMapper.getAllConfig();
        sysConfigList = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.PROM)).collect(Collectors.toList());
        addPublishMessage(sysConfigList);
        sortConfig(sysConfigList);
        List<SysConfig> openGauss = sysConfigList.stream().filter(item -> Constants.PROM.equals(item.getPlatform())).collect(Collectors.toList());
        List<SysConfig> result = sysConfigList.stream().filter(item -> Constants.PROM.equals(item.getPlatform())).skip((page - 1) * size).limit(size).collect(Collectors.toList());
        addChildren(result);
        return ResponseVO.pageResponseVO(openGauss.size(), result);
    }

    @Override
    public ResponseVO getDataSourceNameList() {
        List<SysConfig> sysConfigList = configMapper.getAllConfig();
        sysConfigList = sysConfigList.stream().filter(item -> item.getPlatform().equals(Constants.PROM)).collect(Collectors.toList());
        List<SourceName> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(sysConfigList)) {
            for (SysConfig sysConfig : sysConfigList) {
                SourceName sourceName = new SourceName();
                if (ObjectUtil.isNotEmpty(sysConfig) && ObjectUtil.isNotEmpty(sysConfig.getConnectName()) && ObjectUtil.isNotEmpty(sysConfig.getDataSourceId())) {
                    sourceName.setConnectName(sysConfig.getConnectName());
                    sourceName.setDataSourceId(sysConfig.getDataSourceId());
                    list.add(sourceName);
                }
            }
        }
        return ResponseVO.successResponseVO(list);
    }

    private void addChildren(List<SysConfig> result) {
        for (SysConfig sysConfig : result) {
            sysConfig.setPassword("");
            List<Long> jobid = sourceTargetMapper.getJobIdBySourceId(sysConfig.getDataSourceId());
            List<SysJob> sysJobs = sysJobMapper.selectBatchJobByIds(jobid);
            sysConfig.setTargets(sysJobs);
        }
    }

    private void addPublishMessage(List<SysConfig> filter) {
        List<SysSourceTarget> sourceTargets = sourceTargetMapper.getAll();
        if (CollectionUtil.isNotEmpty(filter)) {
            for (SysConfig sysConfig : filter) {
                for (SysSourceTarget sysSourceTarget : sourceTargets) {
                    if (sysConfig.getDataSourceId().equals(sysSourceTarget.getDataSourceId())) {
                        sysConfig.setLastReleaseTime(sysSourceTarget.getLastReleaseTime());
                        sysConfig.setJobIds(sysSourceTarget.getJobIds());
                    }
                }
            }
        }
    }


    private List<SysConfig> filterConfig(List<SysConfig> sysConfigList, String searchValue) {
        List<SysConfig> result = new ArrayList<>();
        List<SysConfig> nameList = sysConfigList.stream().filter(orderInfo -> Boolean.FALSE ? orderInfo.getConnectName().equals(searchValue) :
                        orderInfo.getConnectName().contains(searchValue))
                .collect(Collectors.toList());
        List<SysConfig> ipList = sysConfigList.stream().filter(orderInfo -> Boolean.FALSE ? orderInfo.getIp().equals(searchValue) :
                        orderInfo.getIp().contains(searchValue))
                .collect(Collectors.toList());
        result.addAll(nameList);
        result.addAll(ipList);
        return result;
    }

    @Override
    public ResponseVO deleteConfigById(List<Long> ids) {
        if (CollectionUtil.isNotEmpty(ids)) {
            for (Long sourceId : ids) {
                try {
                    if (ObjectUtil.isNotEmpty(sourceId)) {
                        SysSourceTarget sourceTarget = new SysSourceTarget();
                        sourceTarget.setDataSourceId(sourceId);
                        List<Long> jobIds = sourceTargetMapper.getJobIdBySourceId(sourceId);
                        if (jobIds != null && CollectionUtil.isNotEmpty(jobIds)) {
                            sourceTarget.setJobIds(jobIds);
                        } else {
                            sourceTarget.setJobIds(new ArrayList<>());
                        }
                        sysJobService.singlePublishPause(sourceTarget);
                        sourceTargetMapper.deleteBySourceId(sourceId);
                    }
                } catch (SchedulerException e) {
                    log.error("deleteConfigById-->{}", e.getMessage());
                }
            }
            configMapper.deleteBatchByIds(ids);
        }
        return ResponseVO.successResponseVO("删除成功");
    }

    @Override
    public ResponseVO selectZabbixConfig() {
        SysConfig sysConfig = configMapper.getZabbixConfig();
        if (sysConfig != null) {
            sysConfig.setPassword("");
        }
        return ResponseVO.successResponseVO(sysConfig);
    }

    @Override
    public ResponseVO selectNagiosConfig() {
        SysConfig sysConfig = configMapper.getNagiosConfig();
        if (sysConfig != null) {
            sysConfig.setClientPassword("");
            sysConfig.setServerPassword("");
        }
        return ResponseVO.successResponseVO(sysConfig);
    }

    private void sortConfig(List<SysConfig> sysConfigList) {
        sysConfigList.sort(Comparator.comparing(SysConfig::getTime).reversed());
    }
}
