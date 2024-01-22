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

package org.opengauss.tun.proloading;

import java.io.File;
import org.opengauss.tun.common.FixedTuning;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * LoadCache
 *
 * @author liu
 * @since 2023-12-20
 */
@Component
@Order(1)
public class InitDb implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Create database files
        File dbFile1 = new File(FixedTuning.TUNING_DB);
        File dbDir = dbFile1.getParentFile();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        if (!dbFile1.exists()) {
            dbFile1.createNewFile();
        }
    }
}
