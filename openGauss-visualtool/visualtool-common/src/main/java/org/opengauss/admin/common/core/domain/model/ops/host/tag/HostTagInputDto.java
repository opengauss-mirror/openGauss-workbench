package org.opengauss.admin.common.core.domain.model.ops.host.tag;

import lombok.Data;

import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:45
 **/
@Data
public class HostTagInputDto {
    private List<String> names;
    private List<String> hostIds;
}
