package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

import java.util.List;

@Data
public class GucSettingDto {
    private String clusterId;
    private String hostId;
    private String dataPath;
    private Boolean isApplyToAllNode;
    private List<GucSettingVO> settings;
}
