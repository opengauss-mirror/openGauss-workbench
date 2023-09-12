/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
        Download.download(HTTP, file.getCanonicalPath());
        assertThrows(RuntimeException.class, () -> {
            Download.download(HTTP, PATH);
        });
        if (file.exists()) {
            file.delete();
        }
    }
}