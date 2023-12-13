/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.manager.factory;

import com.jcraft.jsch.Session;
import java.util.TimerTask;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.service.impl.SqlOperationImpl;
import org.opengauss.collect.utils.MonitSpringUtils;

/**
 * AsyncFactory
 *
 * @author liu
 * @since 2022-10-01
 */
public class AsyncFactory {
    /**
     * updateTaskStatus
     *
     * @param period period
     * @return TimerTask
     */
    public static TimerTask updateTaskStatus(final CollectPeriod period) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(SqlOperationImpl.class).updateCurrentStatus(period);
            }
        };
    }

    /**
     * executeCommand
     *
     * @param session session
     * @param command command
     * @return TimerTask TimerTask
     */
    public static TimerTask executeCommand(final Session session, final String command) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(SqlOperationImpl.class).runCommand(session, command);
            }
        };
    }
}
