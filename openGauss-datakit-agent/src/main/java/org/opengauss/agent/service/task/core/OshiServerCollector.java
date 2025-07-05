/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.service.task.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import org.opengauss.agent.entity.HostBaseInfo;
import org.opengauss.agent.utils.MathUtils;
import org.opengauss.agent.vo.MultiValueMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OshiServerCollector
 *
 * @author: wangchao
 * @Date: 2025/4/8 17:04
 * @Description: OshiServerCollector
 * @since 7.0.0-RC2
 **/
@Slf4j
public class OshiServerCollector {
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final CentralProcessor CPU = SYSTEM_INFO.getHardware().getProcessor();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();
    private static final GlobalMemory MEMORY = SYSTEM_INFO.getHardware().getMemory();
    private static final List<HWDiskStore> DISKS = SYSTEM_INFO.getHardware().getDiskStores();
    private static final List<OSFileStore> FILE_STORES = SYSTEM_INFO.getOperatingSystem()
        .getFileSystem()
        .getFileStores();
    private static final List<NetworkIF> NETWORK_IFS = SYSTEM_INFO.getHardware().getNetworkIFs();

    private final List<DynamicCollector> collectors = Arrays.asList(new MemoryTotalCollector(), new CpuUsageCollector(),
        new MemoryAvailableCollector(), new MemoryUsageCollector());
    private final List<MultiDynamicCollector> multiCollectors = Arrays.asList(new NetMultiDynamicCollector(),
        new DiskMultiDynamicCollector(), new FileStoreMultiDynamicCollector());

    /**
     * collect host base info
     *
     * @return HostBaseInfo
     */
    public static HostBaseInfo fixedCollect() {
        // 主机名
        String hostName = OS.getNetworkParams().getHostName();
        // CPU 信息
        String cpuModel = CPU.getProcessorIdentifier().getName();
        long cpuFreq = CPU.getProcessorIdentifier().getVendorFreq();
        int physicalCores = CPU.getPhysicalProcessorCount();
        int logicalCores = CPU.getLogicalProcessorCount();
        String architecture = CPU.getProcessorIdentifier().getMicroarchitecture();
        // 操作系统信息
        String osName = OS.getFamily();
        String osVersion = OS.getVersionInfo().getVersion();
        String osBuild = OS.getVersionInfo().getBuildNumber();
        return HostBaseInfo.builder()
            .hostName(hostName)
            .cpuModel(cpuModel)
            .cpuFreq(cpuFreq)
            .cpuArchitecture(architecture)
            .physicalCores(physicalCores)
            .logicalCores(logicalCores)
            .osName(osName)
            .osVersion(osVersion)
            .osBuild(osBuild)
            .build();
    }

    /**
     * collect dynamic info
     *
     * @param collectorList collector list
     * @return dynamic info
     */
    public Map<String, Double> dynamicCollectMap(List<String> collectorList) {
        Map<String, Double> result = new HashMap<>();
        collectors.stream().filter(collector -> collectorList.contains(collector.getName())).forEach(collector -> {
            result.put(collector.getName(), collector.collect().getValue());
        });
        return result;
    }

    /**
     * multi dynamic collect
     *
     * @param collectorList collector list
     * @return multi value metric list
     */
    public List<MultiValueMetric> multiDynamicCollect(List<String> collectorList) {
        List<MultiValueMetric> result = new ArrayList<>();
        multiCollectors.stream().filter(collector -> collector.containsAny(collectorList)).forEach(collector -> {
            result.addAll(collector.collect());
        });
        return result;
    }

    /**
     * dynamic collect interface
     */
    private interface DynamicCollector {
        /**
         * dynamic collect
         *
         * @return result
         */
        Pair<String, Double> collect();

        /**
         * collector metric name
         *
         * @return name
         */
        String getName();
    }

    /**
     * multi dynamic collect interface
     */
    private interface MultiDynamicCollector {
        /**
         * collect data
         *
         * @return result
         */
        List<MultiValueMetric> collect();

        /**
         * check target names contains any metric name in collector
         *
         * @param targetNames target names
         * @return true if contains any metric name
         */
        boolean containsAny(List<String> targetNames);
    }

    /**
     * Net dynamic collector
     */
    private static class NetMultiDynamicCollector implements MultiDynamicCollector {
        private static final int INTERVAL_MILLIS = 1000;
        private static final List<String> NAMES = List.of("system.net.bytes.received", "system.net.bytes.sent");

        @Override
        public List<MultiValueMetric> collect() {
            // 记录初始值
            long[] initialRecv = new long[NETWORK_IFS.size()];
            long[] initialSent = new long[NETWORK_IFS.size()];
            for (int i = 0; i < NETWORK_IFS.size(); i++) {
                NetworkIF netIf = NETWORK_IFS.get(i);
                if (!netIf.isConnectorPresent() || netIf.getName().startsWith("lo")) {
                    continue;
                }
                netIf.updateAttributes();
                initialRecv[i] = netIf.getBytesRecv();
                initialSent[i] = netIf.getBytesSent();
            }
            // 等待采样间隔
            Util.sleep(INTERVAL_MILLIS);
            List<MultiValueMetric> list = new ArrayList<>();
            // 计算流量差值
            for (int i = 0; i < NETWORK_IFS.size(); i++) {
                NetworkIF netIf = NETWORK_IFS.get(i);
                if (!netIf.isConnectorPresent() || netIf.getName().startsWith("lo")) {
                    continue;
                }
                netIf.updateAttributes();
                double recv = MathUtils.divide1024(netIf.getBytesRecv() - initialRecv[i]);
                double sent = MathUtils.divide1024(netIf.getBytesSent() - initialSent[i]);
                list.add(MultiValueMetric.of(getName(0), netIf.getName(), recv));
                list.add(MultiValueMetric.of(getName(1), netIf.getName(), sent));
            }
            return list;
        }

        private String getName(int index) {
            return NAMES.get(index);
        }

        @Override
        public boolean containsAny(List<String> targetNames) {
            if (CollUtil.isEmpty(targetNames)) {
                return false;
            }
            return !Collections.disjoint(NAMES, targetNames);
        }
    }

    /**
     * Disk dynamic collector
     */
    private static class DiskMultiDynamicCollector implements MultiDynamicCollector {
        private static final List<String> NAMES = List.of("system.disk.bytes.read", "system.disk.bytes.write",
            "system.disk.total");

        @Override
        public List<MultiValueMetric> collect() {
            List<MultiValueMetric> list = new ArrayList<>();
            DISKS.forEach(disk -> {
                String diskName = disk.getName();
                list.add(MultiValueMetric.of(getName(0), diskName, disk.getWriteBytes() / 1e9));
                list.add(MultiValueMetric.of(getName(1), diskName, disk.getReadBytes() / 1e9));
                list.add(MultiValueMetric.of(getName(2), diskName, disk.getSize() / 1e9));
            });
            return list;
        }

        private String getName(int index) {
            return NAMES.get(index);
        }

        @Override
        public boolean containsAny(List<String> targetNames) {
            if (CollUtil.isEmpty(targetNames)) {
                return false;
            }
            return !Collections.disjoint(NAMES, targetNames);
        }
    }

    /**
     * FileStore dynamic collector
     */
    private static class FileStoreMultiDynamicCollector implements MultiDynamicCollector {
        private static final List<String> NAMES = List.of("system.disk.fs.total", "system.disk.fs.used",
            "system.disk.fs.free");

        @Override
        public List<MultiValueMetric> collect() {
            List<MultiValueMetric> list = new ArrayList<>();
            FILE_STORES.forEach(fs -> {
                String mountPoint = fs.getMount().replace("\\", "");
                list.add(MultiValueMetric.of(getName(0), mountPoint, fs.getTotalSpace() / 1e9));
                list.add(MultiValueMetric.of(getName(1), mountPoint, fs.getUsableSpace() / 1e9));
                list.add(MultiValueMetric.of(getName(2), mountPoint, fs.getFreeSpace() / 1e9));
            });
            return list;
        }

        private String getName(int index) {
            return NAMES.get(index);
        }

        @Override
        public boolean containsAny(List<String> targetNames) {
            if (CollUtil.isEmpty(targetNames)) {
                return false;
            }
            return !Collections.disjoint(NAMES, targetNames);
        }
    }

    /**
     * CpuUsageCollector
     */
    private static class CpuUsageCollector implements DynamicCollector {
        private static final int INTERVAL_MILLIS = 1000;

        @Override
        public Pair<String, Double> collect() {
            // 第一次采样（必须初始化）
            long[][] prevTicks = CPU.getProcessorCpuLoadTicks();
            try {
                Thread.sleep(INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 第二次采样并计算
            double[] load = CPU.getProcessorCpuLoadBetweenTicks(prevTicks);
            double totalUsage = calculateTaskManagerStyle(load);
            return new Pair(getName(), totalUsage);
        }

        /**
         * Calculate the total utilization rate according to the logic of the task manager
         *
         * @param perCoreLoad Utilization array for each logical core (0.0-1.0)
         * @return Total utilization percentage
         */
        private static double calculateTaskManagerStyle(double[] perCoreLoad) {
            double sum = 0.0;
            for (double core : perCoreLoad) {
                sum += core;
            }
            // Key formula: Total utilization rate=Sum of utilization rates of each core/Number of logical cores * 100
            return (sum / Runtime.getRuntime().availableProcessors()) * 100;
        }

        @Override
        public String getName() {
            return "system.cpu.usage";
        }
    }

    /**
     * MemoryTotalCollector
     */
    private static class MemoryTotalCollector implements DynamicCollector {
        @Override
        public Pair<String, Double> collect() {
            return new Pair<>(getName(), MathUtils.convertBytesToGigabytes(MEMORY.getTotal()));
        }

        @Override
        public String getName() {
            return "system.memory.total";
        }
    }

    /**
     * MemoryAvailableCollector
     */
    private static class MemoryAvailableCollector implements DynamicCollector {
        @Override
        public Pair<String, Double> collect() {
            return new Pair(getName(), MathUtils.convertBytesToGigabytes(MEMORY.getAvailable()));
        }

        @Override
        public String getName() {
            return "system.memory.available";
        }
    }

    /**
     * MemoryUsageCollector
     */
    private static class MemoryUsageCollector implements DynamicCollector {
        @Override
        public Pair<String, Double> collect() {
            return new Pair(getName(),
                MathUtils.calculateUtilizationPercentage(MEMORY.getAvailable(), MEMORY.getTotal()));
        }

        @Override
        public String getName() {
            return "system.memory.usage";
        }
    }
}
