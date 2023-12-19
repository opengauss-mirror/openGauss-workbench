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
 *  EbpfSendFileHandler.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/handler/EbpfSendFileHandler.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constant.CommonConstants;
import com.nctigba.ebpf.constant.EbpfTypeConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.util.HTTPUtils;
import com.nctigba.ebpf.util.OSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

/**
 * ebpf/os action
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Component
@Slf4j
public class EbpfSendFileHandler {

    @Autowired
    UrlConfig urlConfig;


    /**
     * data sendFile
     *
     * @param taskid taskid
     * @param monitorType monitorType
     */
    public void sendFile(String taskid, String monitorType) {
        FileSystemResource file = null;
        HTTPUtils httpUtils = new HTTPUtils();
        String outputUrl = System.getProperty("user.dir") + "/output/";
        String httpUrl = urlConfig.getHttpUrl();
        String url = httpUrl.substring(0, httpUrl.lastIndexOf(CommonConstants.SLASH) + 1) + taskid + httpUrl.substring(
                httpUrl.lastIndexOf(CommonConstants.SLASH));
        try {
            if (EbpfTypeConstants.PROFILE.equals(monitorType) || EbpfTypeConstants.OFFCPUTIME.equals(monitorType)
                    || EbpfTypeConstants.MEMLEAK.equals(monitorType)) {
                if (new FileSystemResource(outputUrl + taskid + monitorType + FileTypeConstants.STACKS).contentLength()
                        >= 1) {
                    file = new FileSystemResource(outputUrl + taskid + monitorType + FileTypeConstants.SVG);
                }
            } else {
                file = new FileSystemResource(outputUrl + taskid + monitorType + FileTypeConstants.DEFAULT);
            }
            boolean isFinish = httpUtils.httpUrlPost(url, file, monitorType);
            if (isFinish) {
                if (file != null && file.exists()) {
                    file.getFile().delete();
                }
                if (EbpfTypeConstants.PROFILE.equals(monitorType) || EbpfTypeConstants.MEMLEAK.equals(monitorType)
                        || EbpfTypeConstants.OFFCPUTIME.equals(monitorType)) {
                    FileSystemResource stackFile = new FileSystemResource(
                            outputUrl + taskid + monitorType + FileTypeConstants.STACKS);
                    if (stackFile.exists()) {
                        stackFile.getFile().delete();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("fail to open file!");
        }
    }

    /**
     * data createSvg
     *
     * @param taskid taskid
     * @param monitorType monitorType
     */
    public void createSvg(String taskid, String monitorType) {
        String svgcmd = null;
        OSUtils osUtils = new OSUtils();
        String ebpfUrl = System.getProperty("user.dir") + "/output/";
        String fgUrl = urlConfig.getFgUrl();
        if (EbpfTypeConstants.PROFILE.equals(monitorType)) {
            svgcmd = "cd " + fgUrl + " &&./flamegraph.pl --title='On-CPU Time' "
                    + ebpfUrl + taskid + monitorType + FileTypeConstants.STACKS + " > " + ebpfUrl + taskid + monitorType
                    + FileTypeConstants.SVG;
        } else if (EbpfTypeConstants.OFFCPUTIME.equals(monitorType)) {
            svgcmd = "cd " + fgUrl + " &&./flamegraph.pl --colors=io --title='Off-CPU Time' "
                    + ebpfUrl + taskid + monitorType + FileTypeConstants.STACKS + " > " + ebpfUrl + taskid + monitorType
                    + FileTypeConstants.SVG;
        } else if (EbpfTypeConstants.MEMLEAK.equals(monitorType)) {
            svgcmd = "cd " + fgUrl
                    + " &&./flamegraph.pl --colors=mem --title='malloc() bytes Flame Graph' --countname=bytes "
                    + ebpfUrl + taskid + monitorType + FileTypeConstants.STACKS + " > " + ebpfUrl + taskid + monitorType
                    + FileTypeConstants.SVG;
        }
        osUtils.exec(svgcmd);
    }
}