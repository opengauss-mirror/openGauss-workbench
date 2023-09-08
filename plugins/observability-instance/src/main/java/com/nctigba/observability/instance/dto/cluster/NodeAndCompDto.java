/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SyncSituation
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class NodeAndCompDto {
    private State cmServerState;
    private State omMonitorState;
    private State cmAgentState;

    /**
     * get NodeAndCompDto
     *
     * @param vo OpsClusterNodeVO
     * @return NodeAndCompDto
     */
    public static NodeAndCompDto of(OpsClusterNodeVO vo) {
        return new NodeAndCompDto();
    }

    public void setCmServerState(String cmServerState) {
        this.cmServerState = new State("OS.bin.state." + BinState.getState(cmServerState));
    }

    public void setOmMonitorState(String omMonitorState) {
        this.omMonitorState = new State("OS.bin.state." + BinState.getState(omMonitorState));
    }

    public void setCmAgentState(String cmAgentState) {
        this.cmAgentState = new State("OS.bin.state." + BinState.getState(cmAgentState));
    }

    /**
     * BinState
     */
    @Getter
    @AllArgsConstructor
    public enum BinState {
        TASK_UNINTERRUPTIBLE("D"),
        TASK_RUNNING("R"),
        TASK_INTERRUPTIBLE("S"),
        TASK_STOPPED("T"),
        TASK_TRACED("t"),
        EXIT_ZOMBIE("Z"),
        EXIT_DEAD("X"),
        STOP("stop"),
        UNKNOWN("Unknown");

        private final String code;

        /**
         * getState
         *
         * @param s String
         * @return BinState
         */
        public static BinState getState(String s) {
            for (BinState state : BinState.values()) {
                if (s.contains(state.code)) {
                    return state;
                }
            }
            return UNKNOWN;
        }
    }
}
