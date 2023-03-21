package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.service.ParamMonitorService;
import com.nctigba.ebpf.util.HTTPUtil;
import com.nctigba.ebpf.util.OSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Async("ebpfPool")
public class ParamMonitorServiceImpl implements ParamMonitorService {

    @Autowired
    UrlConfig urlConfig;

    @Override
    public void getOsParamData(String tid, String taskid, String monitorType) {
        OSUtil osUtil = new OSUtil();
        HTTPUtil httpUtil = new HTTPUtil();
        String httpUrl = urlConfig.getHttpUrl();
        String outputUrl = urlConfig.getOutputUrl();
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskid + monitorType;
        String url = httpUrl.substring(0, httpUrl.lastIndexOf("/") + 1) + taskid + httpUrl.substring(httpUrl.lastIndexOf("/"));
        String cmd = "sysctl -a " + fileUrl + FileType.DEFAULT;
        osUtil.exec(cmd);
        FileSystemResource file = new FileSystemResource(outputUrl + taskid + monitorType + FileType.DEFAULT);
        httpUtil.httpUrlPost(url, file, monitorType);
    }
}
