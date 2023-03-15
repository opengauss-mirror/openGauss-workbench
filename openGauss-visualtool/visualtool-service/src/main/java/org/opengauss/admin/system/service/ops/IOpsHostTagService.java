package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;

/**
 * @author lhf
 * @date 2023/3/14 23:36
 **/
public interface IOpsHostTagService extends IService<OpsHostTagEntity> {
    void addTag(HostTagInputDto hostTagInputDto);
}
