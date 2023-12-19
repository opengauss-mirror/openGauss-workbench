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
 *  DownloadTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/util/DownloadTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * DownloadTest.java
 *
 * @since 2023年7月17日
 */
class DownloadTest {
    private static final String HTTP = "http://www.baidu.com";
    private static final String PATH = "1.tmp";

    @Test
    void test() throws IOException {
        var file = new File(PATH);
        if (file.exists()) {
            file.delete();
        }
        DownloadUtils.download(HTTP, file.getCanonicalPath());
        assertThrows(RuntimeException.class, () -> {
            DownloadUtils.download(HTTP, PATH);
        });
        if (file.exists()) {
            file.delete();
        }
    }
}