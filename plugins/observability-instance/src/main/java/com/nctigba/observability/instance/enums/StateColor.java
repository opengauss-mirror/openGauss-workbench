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
 *  StateColor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/enums/StateColor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * StateColor.java
 *
 * @since 2023-08-25
 */
@RequiredArgsConstructor
@Getter
public enum StateColor {
    GREEN("cluster.state.value.Normal", "cluster.node.state.Normal", "cluster.node.syncState.Streaming",
            "OS.bin.state.TASK_RUNNING"),
    YELLOW("cluster.state.value.Degraded", "cluster.node.state.Need repair", "cluster.node.state.Starting",
            "cluster.node.state.Wait promoting", "cluster.node.state.Promoting", "cluster.node.state.Demoting",
            "cluster.node.state.Building", "cluster.node.state.Catchup", "cluster.node.state.Coredump",
            "cluster.node.syncState.Catchup", "OS.bin.state.TASK_INTERRUPTIBLE", "OS.bin.state.TASK_STOPPED",
            "OS.bin.state.TASK_TRACED", "OS.bin.state.TASK_UNINTERRUPTIBLE"),
    RED("cluster.state.value.Unavailable", "cluster.node.state.Unknown", "OS.bin.state.EXIT_ZOMBIE",
            "OS.bin.state.EXIT_DEAD", "OS.bin.state.STOP", "cluster.node.state.Exception",
            "cluster.node.syncState.Delay", "cluster.node.state.stopped"),
    GREY("cluster.state.value.Unknown", "OS.bin.state.UNKNOWN", "cluster.node.syncState.Unknown");

    private String[] state;

    StateColor(String... states) {
        this.state = states;
    }

    /**
     * getColor
     *
     * @param state state
     * @return color
     */
    public static StateColor getColor(String state) {
        for (StateColor color : StateColor.values()) {
            Set<String> states = Arrays.stream(color.state).collect(Collectors.toSet());
            if (states.contains(state)) {
                return color;
            }
        }
        return GREY;
    }
}
