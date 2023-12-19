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
 *  CommandController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/controller/CommandController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.service.TargetService;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.DbUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Top process and thread Controller
 *
 * @since 2023/12/1
 */
@RestController
@RequestMapping("/cmd")
@AllArgsConstructor
public class CommandController {
    private static final String PID = "PID";
    private static final String TOP = "top -b -n 1";
    private static final String TOP_DB_PID = "netstat -nap|grep :'|grep LISTEN";
    private static final String TOP_DB_THREAD = "top -H -bn1 -p ";
    private static final String PS = "ps -aux";
    private static final String NETSTAT = "netstat -tulnp|grep gauss";
    private static final String TOTAL_MEMORY = "grep MemTotal /proc/meminfo | awk '{print $2 * 1024}'";
    private final DbUtils dbUtil;
    @Autowired
    private final TargetService targetService;

    private String getDbPort(String nodeId) {
        Optional<TargetConfig> opTarget =
                targetService.getTargetConfigs().stream().filter(z -> z.getNodeId().equals(nodeId)).findFirst();
        if (!opTarget.isPresent()) {
            throw new CollectException("No match node id target config for node Id:" + nodeId);
        }
        return opTarget.get().getDbPort();
    }

    /**
     * Get top process and thread for the system where database on
     *
     * @param nodeId node Id for database
     * @param sort   sort field
     * @return table
     * @throws IOException Read file error
     * @since 2023/12/1
     */
    @GetMapping("/top")
    public Object top(
            @RequestParam String nodeId,
            @RequestParam(required = false) String sort) throws IOException {
        List<String> header = new ArrayList<>();
        List<Map<String, String>> top = new ArrayList<>();
        CmdUtils.readFromCmd(nodeId, TOP, (i, line) -> {
            if (i < 6) return;
            String[] part = StringUtils.splitByBlank(line);
            if (i == 6) {
                Collections.addAll(header, part);
                return;
            }
            Map<String, String> obj = new HashMap<>();
            for (int j = 0; j < header.size(); j++) {
                obj.put(header.get(j), part[j]);
            }
            top.add(obj);
        });
        // db thread pids
        Set<String> pids = new HashSet<>();
        CmdUtils.readFromCmd(nodeId, TOP_DB_PID.replaceAll("'", getDbPort(nodeId)), line -> {
            var part = StringUtils.splitByBlank(line);
            var pid = part[part.length - 1];
            pid = pid.split("/")[0];
            pids.add(pid);
        });
        // full command
        CmdUtils.readFromCmd(nodeId, PS, line -> {
            var part = StringUtils.splitByBlank(line);
            for (Map<String, String> map : top) {
                if (map.get(PID).equals(part[1])) {
                    map.put("FullCommand",
                            StrUtil.join(StrUtil.SPACE, (Object[]) ArrayUtil.sub(part, 10, part.length - 1)));
                    return;
                }
            }
        });
        // db port
        // eg: tcp 0 0 0.0.0.0:5432 0.0.0.0:* LISTEN 3825204/gaussdb
        Map<String, Set<String>> ports = new HashMap<>();
        CmdUtils.readFromCmd(nodeId, NETSTAT, (i, line) -> {
            if (i < 2 && !line.startsWith("tcp")) {
                return;
            }
            var part = StringUtils.splitByBlank(line);
            if (part.length < 7) {
                return;
            }
            // 5432
            String[] ipPort = part[3].split(StrUtil.COLON);
            // 3825204, 5432
            String key = part[6].split(StrUtil.SLASH)[0];
            if (!ports.containsKey(key)) {
                ports.put(key, new HashSet<>());
            }
            ports.get(key).add(ipPort[ipPort.length - 1]);
        });
        top.forEach(map -> {
            String pid = map.get(PID);
            if ("gaussdb".equals(map.get("COMMAND")) && ports.containsKey(pid)) {
                map.put("port", String.valueOf(ports.get(pid).stream().mapToInt(Integer::parseInt).min().orElse(0)));
            }
        });

        // thread memory
        BigDecimal totalMemory = new BigDecimal(CmdUtils.readFromCmd(nodeId, TOTAL_MEMORY)).divide(new BigDecimal(100));
        List<Map<String, Object>> memList = dbUtil.query(nodeId, "select s.lwtid , sum(usedsize) used "
                + "from GS_SESSION_MEMORY_DETAIL d, dbe_perf.thread_wait_status s "
                + "where d.sessid like '%.' || s.tid group by s.lwtid,d.sessid");
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        Map<String, Object> memMap = memList.stream().collect(Collectors.toMap(map -> map.get("lwtid").toString(),
                map -> decimalFormat.format(
                        new BigDecimal(map.get("used")
                                .toString()).divide(
                                totalMemory, 2,
                                RoundingMode.HALF_UP))));
        // thread
        // e.g. 3825204 omm 20 0 7461264 490508 170264 S 0.0 3.1 2:32.81 gaussdb
        List<Map<String, String>> db_top = new ArrayList<>();
        // thread session/query id
        List<Map<String, Object>> sessions = dbUtil.query(nodeId, "select s.lwtid , a.sessionid , a.query_id "
                + "from pg_stat_activity a, dbe_perf.thread_wait_status s where a.sessionid = s.sessionid");
        if (pids.size() != 0) {
            var pid = pids.iterator().next();
            CmdUtils.readFromCmd(nodeId, TOP_DB_THREAD + pid, (i, line) -> {
                if (i < 6) {
                    return;
                }
                String[] part = StringUtils.splitByBlank(line);
                if (i == 6) {
                    header.clear();
                    Collections.addAll(header, part);
                    return;
                }
                Map<String, String> obj = new HashMap<>();
                for (int j = 0; j < header.size(); j++) {
                    obj.put(header.get(j), part[j]);
                }
                Map<String, Map<String, Object>> sessionMap =
                        sessions.stream().collect(Collectors.toMap(
                                map -> map.get("lwtid").toString(), map -> map));
                if (obj.containsKey(PID)) {
                    var dbTid = obj.get(PID).toString();
                    if (sessionMap.containsKey(dbTid)) {
                        sessionMap.get(dbTid).forEach((key, value) -> {
                            obj.put(key, value.toString());
                        });
                    }
                    obj.put("%MEM", memMap.getOrDefault(dbTid, "0").toString());
                }
                db_top.add(obj);
            });
        }

        if (StrUtil.isNotBlank(sort)) {
            var comparator = new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    try {
                        return -Double.compare(Double.parseDouble(o1.get(sort)), Double.parseDouble(o2.get(sort)));
                    } catch (NumberFormatException e) {
                        return -o1.get(sort).compareTo(o2.get(sort));
                    }
                }
            };
            Collections.sort(top, comparator);
            Collections.sort(db_top, comparator);
        }
        return Arrays.asList(top, db_top);
    }
}