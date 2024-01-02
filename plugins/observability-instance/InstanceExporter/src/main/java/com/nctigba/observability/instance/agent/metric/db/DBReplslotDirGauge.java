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
 *  DBReplslotDirGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/DBReplslotDirGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.db;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.DbUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.DBMetric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collect the occupied size of the pg_replslot directory
 *
 * @since 2023/11/22 15:29
 */
@Service
@Slf4j
@Data
public class DBReplslotDirGauge implements DBMetric {
    private static final String DATADIR_SQL = "SHOW data_directory";
    private static final String COMMAND = "du -s ";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "db_replslot_dir";
    private String[] names = {"db_replslot_dir_size_kbytes"};
    private String[] helps = {"Database replslot dir size."};
    private String[] labelNames = {"host"};

    @Autowired
    private DbUtils dbUtils;

    @Override
    public List<List<MetricResult>> collectData(
        CollectTargetDTO target, CollectParamDTO param) throws CollectException {
        List<Map<String, Object>> query = dbUtils.query(target.getTargetConfig().getNodeId(), DATADIR_SQL);
        if (CollectionUtil.isEmpty(query)) {
            return new ArrayList<>();
        }
        List<List<MetricResult>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        String baseDir = query.get(0).get("data_directory").toString();
        String dir = baseDir + "/pg_replslot";
        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), COMMAND + dir, line -> {
                if (StrUtil.isBlank(line)) {
                    return;
                }
                String[] part = StringUtils.splitByBlank(line);
                String[] labels = {target.getTargetConfig().getHostId()};
                result.get(0).add(new MetricResult(labels, Double.valueOf(part[0])));
            });
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }
        return result;
    }
}
