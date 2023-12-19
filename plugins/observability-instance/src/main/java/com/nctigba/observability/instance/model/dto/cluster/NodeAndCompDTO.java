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
 *  NodeAndCompDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/cluster/NodeAndCompDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

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
public class NodeAndCompDTO {
    private StateDTO cmServerState;
    private StateDTO omMonitorState;
    private StateDTO cmAgentState;

    /**
     * get NodeAndCompDTO
     *
     * @param vo OpsClusterNodeVO
     * @return NodeAndCompDTO
     */
    public static NodeAndCompDTO of(OpsClusterNodeVO vo) {
        return new NodeAndCompDTO();
    }

    public void setCmServerState(String cmServerState) {
        this.cmServerState = new StateDTO("OS.bin.state." + BinState.getState(cmServerState));
    }

    public void setOmMonitorState(String omMonitorState) {
        this.omMonitorState = new StateDTO("OS.bin.state." + BinState.getState(omMonitorState));
    }

    public void setCmAgentState(String cmAgentState) {
        this.cmAgentState = new StateDTO("OS.bin.state." + BinState.getState(cmAgentState));
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
