package com.tools.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.tools.monitor.common.contant.ConmmonShare;
import com.tools.monitor.entity.Prom;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.service.MeterService;
import com.tools.monitor.util.HandleUtils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.plugin.facade.MonitorToolsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MeterServiceImpl
 */
@Slf4j
@Service
public class MeterServiceImpl implements MeterService {
    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    @Autowired
    public CollectorRegistry collectorRegistry;

    @Autowired
    private NagiosServiceImpl nagiosServiceImpl;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    public Gauge my_library_transactions_active;

    public List<Prom> list = new ArrayList<>();

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private MonitorToolsFacade monitorToolsService;


    /**
     * publish
     *
     * @param list list
     * @param sysConfig sysConfig
     */
    public void publish(List<Map<String, Object>> list, SysConfig sysConfig, String task, SysJob sysJob) {
        synchronized (this) {
            String name = sysConfig.getConnectName();
            for (Map<String, Object> maps : list) {
                for (Map.Entry<String, Object> entry : maps.entrySet()) {
                    if (ObjectUtil.isEmpty(entry.getValue()) && !entry.getKey().equalsIgnoreCase("toastsize")) {
                        maps.put(entry.getKey(), "default");
                    }
                    if (ObjectUtil.isEmpty(entry.getValue()) && entry.getKey().equalsIgnoreCase("toastsize")) {
                        maps.put(entry.getKey(), "0");
                    }
                    if (entry.getValue().toString().startsWith(".")) {
                        String value = "0" + entry.getValue().toString();
                        maps.put(entry.getKey(), value);
                    }
                }
            }
            Map<String, Object> nagiosMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> arry = list.get(i);
                Map<String, Object> metric = HandleUtils.getMap(arry);
                dealMetric(metric, i);
                String[] key = getKey(metric);
                String[] value = getValue(metric);
                for (Map.Entry<String, Object> entry : arry.entrySet()) {
                    if (entry.getValue().toString().matches(ISNUM) || entry.getValue().toString().matches(KEXUE)) {
                        if (!sysJob.getPlatform().equals(ConmmonShare.NAGIOS)) {
                            monitorToolsService.reportRegister(entry.getKey() + "_" + task + "_" + name, entry.getValue(), key, value);
                        }
                        nagiosMap.put(entry.getKey() + "_" + task + "_" + name + "_" + i, entry.getValue());
                    }
                }
            }
            if (sysJob.getPlatform().equals(ConmmonShare.NAGIOS)) {
                reportNagios(nagiosMap);
            }
        }
    }

    private void report(String metricKey, Object metricValue, String[] key, String[] value) {
        if (CollectionUtil.isEmpty(list)) {
            my_library_transactions_active = Gauge.build()
                    .name(metricKey)
                    .help("Active transactions.")
                    .labelNames(key)
                    .register(collectorRegistry);
            Prom prom01 = new Prom(my_library_transactions_active, metricKey);
            list.add(prom01);
        } else {
            Prom prom = list.stream().filter(item -> item.getGaugeName().equals(metricKey)).findFirst().orElse(null);
            if (null != prom) {
                my_library_transactions_active = prom.getGaugs();
            } else {
                my_library_transactions_active = Gauge.build()
                        .name(metricKey)
                        .help("Active transactions.")
                        .labelNames(key)
                        .register(collectorRegistry);
                Prom prom02 = new Prom(my_library_transactions_active, metricKey);
                list.add(prom02);
            }
        }
        String num = new BigDecimal(metricValue.toString()).toPlainString();
        my_library_transactions_active.labels(value).set(Double.valueOf(num));
    }

    /**
     * reportNagios
     *
     * @param nagiosMap nagiosMap
     */
    public void reportNagios(Map<String, Object> nagiosMap) {
        synchronized (this) {
            SysConfig sysConfig = sysConfigMapper.getNagiosConfig();
            if (ObjectUtil.isEmpty(sysConfig)) {
                return;
            }
            if (CollectionUtil.isEmpty(nagiosMap)) {
                return;
            }
            nagiosServiceImpl.writeSh(nagiosMap, sysConfig);
        }
    }

    /**
     * dealMetric
     * @param metric metric
     * @param i i
     */
    public void dealMetric(Map<String, Object> metric, int i) {
        if (CollectionUtil.isEmpty(metric)) {
            metric.put("instance", "node" + i);
        }
    }

    /**
     * getValue
     *
     * @param metric metric
     * @return String
     */
    public String[] getValue(Map<String, Object> metric) {
        Object[] objects = metric.values().toArray(new Object[metric.values().size()]);
        return Arrays.stream(objects).map(Object::toString).toArray(String[]::new);
    }

    /**
     * getKey
     *
     * @param metric metric
     * @return String
     */
    public String[] getKey(Map<String, Object> metric) {
        return metric.keySet().toArray(new String[metric.keySet().size()]);
    }

    /**
     * removeRegister
     *
     * @param gaugeName gaugeName
     */
    public void removeRegister(List<String> gaugeName) {
        monitorToolsService.removeRegister(gaugeName);
    }

    /**
     * remove
     *
     * @param gaugeName gaugeName
     */
    public void remove(List<String> gaugeName) {
        try {
            Thread.sleep(6000);
            if (CollectionUtil.isNotEmpty(gaugeName)) {
                for (String str : gaugeName) {
                    Prom prom = list.stream().filter(item -> str.equals(item.getGaugeName())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(prom)) {
                        list.remove(prom);
                        collectorRegistry.unregister(prom.getGaugs());
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("removeAll-->{}", e.getMessage());
        }
    }
}
