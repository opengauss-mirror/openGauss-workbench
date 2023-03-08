package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/8 15:39
 **/
public interface IHostUserService extends IService<OpsHostUserEntity> {
    List<OpsHostUserEntity> listHostUserByHostId(String hostId);

    boolean removeByHostId(String hostId);

    List<OpsHostUserEntity> listHostUserByHostIdList(List<String> hostIdList);

    boolean add(HostUserBody hostUserBody);

    boolean edit(String hostUserId, HostUserBody hostUserBody);

    boolean del(String hostUserId);

    OpsHostUserEntity getOmmUserByHostId(String hostId);

    OpsHostUserEntity getRootUserByHostId(String hostId);

    void cleanPassword(String hostUserId);
}
