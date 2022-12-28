package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/7 22:22
 **/
@Data
public class ListDir {
    private String path;
    private List<HostFile> files;
}
