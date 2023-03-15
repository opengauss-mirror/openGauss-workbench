package org.opengauss.admin.common.core.domain.model.ops.host.tag;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:45
 **/
@Data
public class HostTagInputDto {
    private List<String> names;
    private List<String> hostIds;

    public static HostTagInputDto of(List<String> tags, String hostId) {
        HostTagInputDto hostTagInputDto = new HostTagInputDto();
        hostTagInputDto.setNames(tags);
        hostTagInputDto.setHostIds(Arrays.asList(hostId));
        return hostTagInputDto;
    }
}
