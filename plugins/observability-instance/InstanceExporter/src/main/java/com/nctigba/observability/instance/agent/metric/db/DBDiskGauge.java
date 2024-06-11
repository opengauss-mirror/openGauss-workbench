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
 *  DBDiskGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/DBDiskGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.db;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.DBMetric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.DbUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DBDiskGauge
 *
 * @since 2023/11/21 17:14
 */
@Service
@Slf4j
@Data
public class DBDiskGauge implements DBMetric {
    private static final String DATADIR_SQL = "SHOW data_directory";
    private static final String ARCHIVE_SQL = "show archive_dest";
    private static final String FILESYSTEM_COMMAND = "df {} | tail -n +2";
    private static final String CM_COMMAND = "cm_ctl list --param --server |grep log_dir | head -n 1";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "db_filesystem";
    private String[] names = {"db_filesystem_size_kbytes", "db_filesystem_used_size_kbytes",
            "db_filesystem_free_size_kbytes"
    };
    private String[] helps = {"Database total filesystem size.", "Database used filesystem size.",
            "Database free filesystem size."};
    private String[] labelNames = {"host", "device", "mount", "dir", "dirType"};

    @Autowired
    private DbUtils dbUtils;

    @Override
    public List<List<MetricResult>> collectData(
            CollectTargetDTO target, CollectParamDTO param) throws CollectException {
        List<List<MetricResult>> result = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            result.add(new ArrayList<>());
        }
        List<Map<String, Object>> query = dbUtils.query(target.getTargetConfig().getNodeId(), DATADIR_SQL);
        if (CollectionUtil.isNotEmpty(query)) {
            String dir = query.get(0).get("data_directory").toString();
            if (StrUtil.isNotEmpty(dir)) {
                collect(target, dir, "dataDir", result);
                collect(target, dir + "/pg_xlog", "xlog", result);
                collect(target, dir + "/pg_log", "pglog", result);
            }
        }
        query.clear();
        query = dbUtils.query(target.getTargetConfig().getNodeId(), ARCHIVE_SQL);
        if (CollectionUtil.isNotEmpty(query)) {
            String dir = query.get(0).get("archive_dest").toString();
            if (StrUtil.isNotEmpty(dir)) {
                collect(target, dir, "archive", result);
            }
        }
        query.clear();
        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), CM_COMMAND, line -> {
                if (StrUtil.isBlank(line)) {
                    return;
                }
                String[] split = line.split("=");
                if (split.length <= 1) {
                    return;
                }
                String dir = split[1].trim();
                if (StrUtil.isNotEmpty(dir)) {
                    collect(target, dir, "cm", result);
                }
            });
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }
        return result;
    }

    private void collect(CollectTargetDTO target, String dir, String dirType,
            List<List<MetricResult>> result) {
        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), StrFormatter.format(FILESYSTEM_COMMAND, dir),
                    line -> {
                        log.debug("agent_filesystem line:{}", line);
                        String[] part = StringUtils.splitByBlank(line);
                        String[] labels = {target.getTargetConfig().getHostId(), part[0], part[5], dir, dirType};
                        result.get(0).add(new MetricResult(labels, Double.parseDouble(part[1])));
                        result.get(1).add(new MetricResult(labels, Double.parseDouble(part[2])));
                        result.get(2).add(new MetricResult(labels, Double.parseDouble(part[3])));
                    });
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }
    }
}
