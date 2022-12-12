package com.nctigba.observability.instance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.common.web.exception.CustomException;
import com.nctigba.observability.instance.dto.server.ServerInfoReq;
import com.nctigba.observability.instance.entity.ServerInfoEntity;
import com.nctigba.observability.instance.factory.OSHandlerFactory;
import com.nctigba.observability.instance.mapper.ServerInfoMapper;
import com.nctigba.observability.instance.service.IServerInfoService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Service implementation class
 * </p>
 *
 * @author auto
 * @since 2022-09-05
 */
@Slf4j
@Service
public class ServerInfoServiceImpl extends ServiceImpl<ServerInfoMapper, ServerInfoEntity> implements IServerInfoService {

    @Autowired
    OSHandlerFactory osHandlerFactory;

    @Override
    public void connectAvailable(ServerInfoReq info) {
        boolean b = osHandlerFactory.getInstance(info.getOs()).testConnectStatus(info.getIp(), info.getPort(), info.getUserName(), info.getUserPassword());
        if (b) {
            log.info("Server connect success ip:[{}] , port:[{}]", info.getIp(), info.getPort());
        } else {
            log.error("Server connect failed ip:[{}] , port:[{}]", info.getIp(), info.getPort());
            throw new CustomException("Server connection failed");
        }
    }

}
