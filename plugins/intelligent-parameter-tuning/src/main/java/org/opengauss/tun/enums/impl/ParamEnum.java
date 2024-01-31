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
            return "是";
        }
    },
    SHARED_BUFFERS {
        @Override
        public String getReal() {
            return "是";
        }
    },
    MAX_PROCESS_MEMORY {
        @Override
        public String getReal() {
            return "是";
        }
    },
    MAINTENANCE_WORK_MEM {
        @Override
        public String getReal() {
            return "否";
        }
    },
    WORK_MEM {
        @Override
        public String getReal() {
            return "否";
        }
    },
    EFFECTIVE_CACHE_SIZE {
        @Override
        public String getReal() {
            return "否";
        }
    },
    EFFECTIVE_IO_CONCURRENCY {
        @Override
        public String getReal() {
            return "否";
        }
    },
    TEMP_BUFFERS {
        @Override
        public String getReal() {
            return "否";
        }
    },
    VACUUM_COST_LIMIT {
        @Override
        public String getReal() {
            return "否";
        }
    },
    VACUUM_COST_PAGE_DIRTY {
        @Override
        public String getReal() {
            return "否";
        }
    },
    VACUUM_COST_PAGE_HIT {
        @Override
        public String getReal() {
            return "否";
        }
    },
    VACUUM_COST_PAGE_MISS {
        @Override
        public String getReal() {
            return "否";
        }
    },
    BACKEND_FLUSH_AFTER {
        @Override
        public String getReal() {
            return "否";
        }
    }
}
