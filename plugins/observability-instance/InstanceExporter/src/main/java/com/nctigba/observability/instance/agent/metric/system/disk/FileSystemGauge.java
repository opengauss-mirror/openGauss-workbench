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
 *  FileSystemGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/disk/FileSystemGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.disk;

import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.DBMetric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * collect the filesystem info
 *
 * @since 2023/9/15 17:17
 */
@Service
@Slf4j
@Data
public class FileSystemGauge implements DBMetric {
    private static final String FILESYSTEM_COMMAND = "df | tail -n +2";
    private static final String FILESYSTEM_INODE_COMMAND = "df -i | tail -n +2";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_filesystem";
    private String[] names = {
            "agent_filesystem_size_kbytes",
            "agent_filesystem_used_size_kbytes",
            "agent_filesystem_free_size_kbytes",
            "agent_filesystem_inode_size",
            "agent_filesystem_inode_used_size",
            "agent_filesystem_inode_free_size"};
    private String[] helps = {
            "file system size.",
            "file system used size.",
            "file system free size.",
            "file system inode total.",
            "file system inode used total.",
            "file system inode free total."};
    private String[] labelNames = {"host", "device", "mount"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            result.add(new ArrayList<>());
        }

        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), FILESYSTEM_COMMAND, line -> {
                log.debug("agent_filesystem line:{}", line);
                String[] part = StringUtils.splitByBlank(line);
                String[] labels = {target.getTargetConfig().getHostId(), part[0], part[5]};
                result.get(0).add(new MetricResult(labels, Double.valueOf(part[1])));
                result.get(1).add(new MetricResult(labels, Double.valueOf(part[2])));
                result.get(2).add(new MetricResult(labels, Double.valueOf(part[3])));
            });
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), FILESYSTEM_INODE_COMMAND, line -> {
                String[] part = StringUtils.splitByBlank(line);
                String[] labels = {target.getTargetConfig().getHostId(), part[0], part[5]};
                result.get(3).add(new MetricResult(labels, Double.valueOf(part[1])));
                result.get(4).add(new MetricResult(labels, Double.valueOf(part[2])));
                result.get(5).add(new MetricResult(labels, Double.valueOf(part[3])));
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}