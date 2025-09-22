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
 * -------------------------------------------------------------------------
 *
 * OnlinePluginloadUtil.java
 *
 * IDENTIFICATION
 *openGauss-datakit/visualtool-service/src/main/java/org/opengauss/admin/system/utils/OnlinePluginloadUtil.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.utils;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.config.SystemConfig;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * OnlinePluginloadUtil
 *
 * @author zhaochen
 * @since 2025-03-20
 * @version 7.0.0-RC1
 */
@Slf4j
@Component
public class OnlinePluginloadUtil {
    /**
     * WsUtil
     */
    @Autowired
    private WsUtil wsUtil;

    /**
     * SysSettingFacade
     */
    @Autowired
    private SysSettingFacade sysSettingFacade;

    /**
     * download plugin
     *
     * @param resourceUrl resourceUrl
     * @param fileName fileName
     * @param wsSession wsSession
     */
    public void download(String resourceUrl, String fileName, WsSession wsSession) {
        DownloadMonitor downloadMonitor = new DownloadMonitor();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String targetPath = SystemConfig.getPluginPath();
        try {
            File existFile = new File(targetPath + File.separator + fileName);
            if (existFile.exists()) {
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
            URLConnection urlConnection = openConnectionWithProxy(url);
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

    private URLConnection openConnectionWithProxy(URL url) throws IOException {
        Proxy proxy = sysSettingFacade.getSysNetProxy();
        if (Objects.isNull(proxy)) {
            return url.openConnection();
        } else {
            return url.openConnection(proxy);
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

        /**
         * download monitor
         *
         * @param bytes byte
         */
        public void count(long bytes) {
            count += bytes;
            long currentPercent = count * 100 / max;
            if (currentPercent > percent) {
                this.percent = currentPercent;
                if (currentPercent % 10 == 0) {
                    log.info("========{}========", this.percent + "%");
                }
                wsUtil.sendText(wsSession, this.percent / 100.00 + "");
            }
        }

        /**
         * end
         */
        public void end() {
            wsUtil.sendText(wsSession, "DOWNLOAD_FINISH");
        }
    }
}
