package com.nctigba.observability.instance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.observability.instance.dto.server.ServerInfoReq;
import com.nctigba.observability.instance.entity.ServerInfoEntity;

/**
 * <p>
 * Service
 * </p>
 *
 * @author liudm@vastdata.com.cn
 * @since 2022-09-05
 */
public interface IServerInfoService extends IService<ServerInfoEntity> {
    /**
     * Test server connectivity
     *
     * @param info server information
     */
    void connectAvailable(ServerInfoReq info);

}
