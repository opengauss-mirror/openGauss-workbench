/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.EbpfType;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.util.HTTPUtil;
import com.nctigba.ebpf.util.OSUtil;
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
public class EbpfSendFileHandler {

    @Autowired
    UrlConfig urlConfig;


    /**
     * data sendFile
     *
     * @param taskid monitorType
     */
    public void sendFile(String taskid, String monitorType) {
        FileSystemResource file;
        HTTPUtil httpUtil = new HTTPUtil();
        String outputUrl = urlConfig.getOutputUrl();
        String httpUrl = urlConfig.getHttpUrl();
        String url = httpUrl.substring(0, httpUrl.lastIndexOf("/") + 1) + taskid + httpUrl.substring(httpUrl.lastIndexOf("/"));
        if (EbpfType.PROFILE.equals(monitorType) || EbpfType.OFFCPUTIME.equals(monitorType) || EbpfType.MEMLEAK.equals(monitorType)) {
            file = new FileSystemResource(outputUrl + taskid + monitorType + FileType.SVG);
        } else {
            file = new FileSystemResource(outputUrl + taskid + monitorType + FileType.DEFAULT);
        }
        httpUtil.httpUrlPost(url, file, monitorType);
    }

    /**
     * data createSvg
     *
     * @param taskid monitorType
     */
    public void createSvg(String taskid, String monitorType) {
        String svgcmd = null;
        OSUtil osUtil = new OSUtil();
        String ebpfUrl = urlConfig.getOutputUrl();
        String fgUrl = urlConfig.getFgUrl();
        if (EbpfType.PROFILE.equals(monitorType)) {
            svgcmd = "cd " + fgUrl + " &&./flamegraph.pl --title='On-CPU Time' "
                    + ebpfUrl + taskid + monitorType + FileType.STACKS + " > " + ebpfUrl + taskid + monitorType + FileType.SVG;
        } else if (EbpfType.OFFCPUTIME.equals(monitorType)) {
            svgcmd = "cd " + fgUrl + " &&./flamegraph.pl --colors=io --title='Off-CPU Time' "
                    + ebpfUrl + taskid + monitorType + FileType.STACKS + " > " + ebpfUrl + taskid + monitorType + FileType.SVG;
        } else if (EbpfType.MEMLEAK.equals(monitorType)) {
            svgcmd = "cd " + fgUrl + " &&./flamegraph.pl --colors=mem --title='malloc() bytes Flame Graph' --countname=bytes "
                    + ebpfUrl + taskid + monitorType + FileType.STACKS + " > " + ebpfUrl + taskid + monitorType + FileType.SVG;
        }
        osUtil.exec(svgcmd);
    }
}
