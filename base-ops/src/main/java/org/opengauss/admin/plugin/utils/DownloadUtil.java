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
