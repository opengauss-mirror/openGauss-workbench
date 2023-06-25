/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengauss.plugin.agent.server.HostMetric;
import org.opengauss.plugin.agent.util.CmdUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/cmd")
@AllArgsConstructor
public class Command {
    private static final String TOP = "top -b -n 1";
    private static final String TOP_DB_PID = "netstat -nap|grep :'|grep LISTEN";
    private static final String TOP_DB_THREAD = "top -H -bn1 -p ";
    private final HostMetric hostMetric;

    @GetMapping("/top")
    public Object top(@RequestParam(required = false) String sort) throws FileNotFoundException, IOException {
        List<String> header = new ArrayList<>();
        List<Map<String, String>> top = new ArrayList<>();
        CmdUtil.readFromCmd(TOP, (i, line) -> {
            if (i < 6)
                return;
            String[] part = StringUtil.splitByBlank(line);
            if (i == 6) {
                Collections.addAll(header, part);
                return;
            }
            Map<String, String> obj = new HashMap<String, String>();
            for (int j = 0; j < header.size(); j++) {
                obj.put(header.get(j), part[j]);
            }
            top.add(obj);
        });
        Set<String> pids = new HashSet<>();
        CmdUtil.readFromCmd(TOP_DB_PID.replaceAll("'", hostMetric.getDbport().toString()), line -> {
            var part = StringUtil.splitByBlank(line);
            var pid = part[part.length - 1];
            pid = pid.split("/")[0];
            pids.add(pid);
        });
        List<Map<String, String>> db_top = new ArrayList<>();
        if (pids.size() != 0) {
            var pid = pids.iterator().next();
            CmdUtil.readFromCmd(TOP_DB_THREAD + pid, (i, line) -> {
                if (i < 6)
                    return;
                String[] part = StringUtil.splitByBlank(line);
                if (i == 6) {
                    header.clear();
                    Collections.addAll(header, part);
                    return;
                }
                Map<String, String> obj = new HashMap<String, String>();
                for (int j = 0; j < header.size(); j++) {
                    obj.put(header.get(j), part[j]);
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