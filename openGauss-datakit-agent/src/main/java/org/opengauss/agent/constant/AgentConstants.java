/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.constant;

/**
 * AgentConstants
 *
 * @author: wangchao
 * @date: 2025/5/8 11:40
 * @since 7.0.0-RC2
 **/
public interface AgentConstants {
    /**
     * heartbeat status up
     */
    String HEARTBEAT_STATUS_UP = "UP";

    /**
     * heartbeat status down
     */
    String HEARTBEAT_STATUS_DOWN = "DOWN";

    /**
     * deviation threshold 50%
     */
    double DEVIATION_THRESHOLD = 0.5;

    /**
     * Duration Timing Constant
     */
    interface Duration {
        /**
         * duration time split T
         */
        String SPLIT_DATE_AND_TIME = "T";

        /**
         * duration time prefix P
         */
        String PREFIX_DATE = "P";

        /**
         * duration time prefix PT
         */
        String PREFIX_ONLE_TIME = "PT";
    }
}
