/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.constants;

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
            "OS.bin.state.EXIT_DEAD", "OS.bin.state.STOP"),
    GREY("cluster.state.value.Unknown", "OS.bin.state.UNKNOWN");

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
