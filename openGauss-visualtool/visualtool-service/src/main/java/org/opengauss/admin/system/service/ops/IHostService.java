package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;

/**
 * @author lhf
 * @date 2022/8/7 22:27
 **/
public interface IHostService extends IService<OpsHostEntity> {
    /**
     * Add host
     *
     * @param hostBody host information
     */
    boolean add(HostBody hostBody);

    /**
     * test connectivity
     *
     * @param hostBody host information
     * @return whether succeed
     */
    boolean ping(HostBody hostBody);

    /**
     * delete host
     *
     * @param hostId HostId
     * @return Whether the deletion is successful
     */
    boolean del(String hostId);

    /**
     * test connectivity
     *
     * @param hostId       host ID
     * @param rootPassword
     * @return whether succeed
     */
    boolean ping(String hostId, String rootPassword);

    /**
     * Edit host information
     *
     * @param hostId   host ID
     * @param hostBody host information
     * @return whether succeed
     */
    boolean edit(String hostId, HostBody hostBody);

    IPage<OpsHostVO> pageHost(Page page, String name);

    void ssh(SSHBody sshBody);
}
