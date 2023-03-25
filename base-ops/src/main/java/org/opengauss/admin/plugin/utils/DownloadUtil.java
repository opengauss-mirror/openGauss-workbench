/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * DownloadUtil.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/utils/DownloadUtil.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/8/15 22:32
 **/
@Slf4j
@Component
public class DownloadUtil {
    @Autowired
    private WsUtil wsUtil;

    public void download(String resourceUrl, String targetPath, String fileName, WsSession wsSession) {
        DownloadMonitor downloadMonitor = new DownloadMonitor();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File existFile = new File(targetPath + "/" + fileName);
            if (existFile.exists()){
                wsUtil.sendText(wsSession, "1");
                return;
            }

            File file = new File(targetPath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new OpsException("Failed to create a directory");
                }
            }

            URL url = new URL(resourceUrl);
            URLConnection urlConnection = url.openConnection();

            long max = urlConnection.getContentLengthLong();
            downloadMonitor.init(max, 0, wsSession);

            inputStream = urlConnection.getInputStream();
            byte[] bs = new byte[10240];

            int len;

            outputStream = new FileOutputStream(targetPath + "/" + fileName, false);

            while ((len = inputStream.read(bs)) != -1) {
                outputStream.write(bs, 0, len);
                downloadMonitor.count(len);
            }
        } catch (Exception e) {
            log.error("File download Failed", e);
            wsUtil.sendText(wsSession, "File download Failed");
        } finally {
            downloadMonitor.end();
            if (Objects.nonNull(outputStream)) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the output stream. Procedure");
                }
            }

            if (Objects.nonNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the output stream. Procedure");
                }
            }
        }
    }

    class DownloadMonitor {
        private long max;
        private long count;
        private long percent;
        private WsSession wsSession;

        public void init(long max, long count, WsSession wsSession) {
            this.max = max;
            this.count = count;
            this.percent = 0;
            this.wsSession = wsSession;
        }

        public void count(long bytes) {
            count += bytes;
            long currentPercent = count * 100 / max;

            if (currentPercent > percent) {
                this.percent = currentPercent;
                log.info("========{}========", this.percent + "%");
                wsUtil.sendText(wsSession, this.percent / 100.00 + "");
            }

        }

        public void end() {
            wsUtil.sendText(wsSession, "DOWNLOAD_FINISH");
        }
    }
}
