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

package org.opengauss.tun.enums.impl;

import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.enums.ParamStrategy;

/**
 * AssessEnum
 *
 * @author liu liu
 * @since 2023-12-05
 */
public enum ParamEnum implements ParamStrategy {
    WAL_BUFFERS {
        @Override
        public String getReal() {
            return FixedTuning.YES;
        }
    },
    SHARED_BUFFERS {
        @Override
        public String getReal() {
            return FixedTuning.YES;
        }
    },
    MAX_PROCESS_MEMORY {
        @Override
        public String getReal() {
            return FixedTuning.YES;
        }
    },
    MAINTENANCE_WORK_MEM {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    WORK_MEM {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    EFFECTIVE_CACHE_SIZE {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    EFFECTIVE_IO_CONCURRENCY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    TEMP_BUFFERS {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    VACUUM_COST_LIMIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    VACUUM_COST_PAGE_DIRTY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    VACUUM_COST_PAGE_HIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    VACUUM_COST_PAGE_MISS {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    BACKEND_FLUSH_AFTER {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_ANALYZE_THRESHOLD {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_NAPTIME {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_VACUUM_COST_DELAY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_VACUUM_COST_LIMIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_VACUUM_THRESHOLD {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    BGWRITER_DELAY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    BGWRITER_FLUSH_AFTER {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    BGWRITER_LRU_MAXPAGES {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    CHECKPOINT_FLUSH_AFTER {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    CHECKPOINT_TIMEOUT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    COMMIT_DELAY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    COMMIT_SIBLINGS {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    DEADLOCK_TIMEOUT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    DEFAULT_STATISTICS_TARGET {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    FROM_COLLAPSE_LIMIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    GEQO_EFFORT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    GEQO_GENERATIONS {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    GEQO_POOL_SIZE {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    GEQO_THRESHOLD {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    JOIN_COLLAPSE_LIMIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    MAX_STACK_DEPTH {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    TEMP_FILE_LIMIT {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    VACUUM_COST_DELAY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    WAL_WRITER_DELAY {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_ANALYZE_SCALE_FACTOR {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    AUTOVACUUM_VACUUM_SCALE_FACTOR {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    BGWRITER_LRU_MULTIPLIER {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    CHECKPOINT_COMPLETION_TARGET {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    CURSOR_TUPLE_FRACTION {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    },
    GEQO_SEED {
        @Override
        public String getReal() {
            return FixedTuning.NO;
        }
    }
}
