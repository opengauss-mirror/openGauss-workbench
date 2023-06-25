/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.util;

import java.io.File;
import java.net.URL;

import cn.hutool.core.io.FileUtil;

public class Download {
    public static File download(String fileUrl, String path) {
        var file = new File(path);
        try {
            var parent = file.getParentFile();
            if (!parent.exists())
                parent.mkdirs();
            var url = new URL(fileUrl);
            var conn = url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            var is = conn.getInputStream();
            FileUtil.writeFromStream(is, file);
            file = file.getCanonicalFile();
        } catch (Exception e) {
            throw new RuntimeException("download fail", e);
        }
        return file;
    }
}