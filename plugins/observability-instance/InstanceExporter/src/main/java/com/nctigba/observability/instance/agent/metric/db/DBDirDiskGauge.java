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
 *  DBDirDiskGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/DBDirDiskGauge.java
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
 * DBDirDiskGauge
 *
 * @author luomeng
 * @since 2024/4/28
 */
@Service
@Slf4j
@Data
public class DBDirDiskGauge implements DBMetric {
    private static final String DATADIR_SQL = "SHOW data_directory";
    private static final String FILESYSTEM_COMMAND =
            "{ df {} | tail -n +2 && du -s {} && readlink $(df {} | tail -n +2 | "
                    + "awk '{print $1}') | awk '{print $NF}';} | tr '\\n' ' '";
    private static final String READ_LINK_CMD = "readlink {} && echo \"\" || echo \"false\"";
    private MetricType type = MetricType.GAUGE;
    private String groupName = "db_dir_filesystem";
    private String[] names = {"db_filesystem_data_used_size_kbytes", "db_filesystem_xlog_used_size_kbytes"};
    private String[] helps = {"Database xlog dir used filesystem size.", "Database data dir used filesystem size."};
    private String[] labelNames = {"host", "device", "dir", "dirType", "part"};
    @Autowired
    private DbUtils dbUtils;

    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target,
            CollectParamDTO param) throws CollectException {
        List<List<MetricResult>> result = new ArrayList<>();
        List<Map<String, Object>> query = dbUtils.query(target.getTargetConfig().getNodeId(), DATADIR_SQL);
        if (CollectionUtil.isEmpty(query)) {
            return result;
        }
        String dir = query.get(0).get("data_directory").toString();
        String xLogDir = dir + "/pg_xlog";
        if (StrUtil.isEmpty(dir)) {
            return result;
        }
        try {
            String lnDir = CmdUtils.readFromCmd(
                    target.getTargetConfig().getNodeId(),
                    StrFormatter.format(READ_LINK_CMD, xLogDir));
            List<MetricResult> dataResult = new ArrayList<>();
            collect(target, dir, "dataDir", dataResult);
            List<MetricResult> xLogResult = new ArrayList<>();
            if (StrUtil.isNotEmpty(lnDir) && !"false".equals(lnDir)) {
                result.add(dataResult);
                collect(target, lnDir, "xlog", xLogResult);
                result.add(xLogResult);
            } else {
                collect(target, xLogDir, "xlog", xLogResult);
                List<MetricResult> newDataResult = new ArrayList<>();
                double newValue = dataResult.get(0).getValue() - xLogResult.get(0).getValue();
                String[] labels = dataResult.get(0).getLabelValues();
                newDataResult.add(new MetricResult(labels, newValue));
                result.add(newDataResult);
                result.add(xLogResult);
            }
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }
        return result;
    }

    private void collect(CollectTargetDTO target, String dir, String dirType,
            List<MetricResult> result) {
        try {
            String cmd = StrFormatter.format(FILESYSTEM_COMMAND, dir, dir, dir);
            CmdUtils.readFromCmd(
                    target.getTargetConfig().getNodeId(), cmd,
                    line -> {
                        log.debug("db_dir_filesystem line:{}", line);
                        String[] part = StringUtils.splitByBlank(line);
                        String[] labels;
                        if (part.length > 8) {
                            labels = new String[]{target.getTargetConfig().getHostId(), part[0], part[7], dirType, part[8].substring(
                                    part[8].indexOf("/") + 1)};
                        } else {
                            labels = new String[]{target.getTargetConfig().getHostId(), part[0], part[7], dirType, ""};
                        }
                        result.add(new MetricResult(labels, Double.parseDouble(part[6])));
                    });
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }
    }
}