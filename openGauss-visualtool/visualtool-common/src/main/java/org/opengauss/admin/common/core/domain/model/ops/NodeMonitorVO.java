package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/10/9 22:40
 **/
@Data
public class NodeMonitorVO {
    private String cpu;
    private String memory;
    private List<NodeNetMonitor> net;
    private String lock;
    private String session;
    private String connectNum;
    private String kernel;
    private String memorySize;
    private List<Map<String, String>> sessionMemoryTop10;
    private String state;
    private Long time;
}
