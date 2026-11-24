/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.tun.enums.impl;

import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.enums.LogPathStategy;

/**
 * LogPathEnum
 *
 * @author liu liu
 * @since 2023-12-05
 */
public enum LogPathEnum implements LogPathStategy {
    DB {
        @Override
        public String getLogPath(String trainingId) {
            return FixedTuning.WORK_PATH + FixedTuning.DB_LOG + trainingId + ".log";
        }
    },
    TUNE {
        @Override
        public String getLogPath(String trainingId) {
            return FixedTuning.WORK_PATH + FixedTuning.TUNE_LOG + trainingId + ".log";
        }
    },
    BENCHMARK {
        @Override
        public String getLogPath(String trainingId) {
            return FixedTuning.WORK_PATH + FixedTuning.BENCHMARK_LOG + trainingId + ".log";
        }
    },
}
