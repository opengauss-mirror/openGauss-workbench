package com.nctigba.observability.instance.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.instance.dto.server.ServerInfoReq;
import com.nctigba.observability.instance.service.IServerInfoService;

/**
 * server information
 *
 * @author liudm@vastdata.com.cn
 * @since 2022-09-05
 */
@RestController
@RequestMapping("/observability/v1/server")
public class ServerInfoController {

    @Autowired
    private IServerInfoService serverInfoService;

    @PostMapping(value = "/connect")
    public AppResult connectAvailable(@RequestBody ServerInfoReq info) {
        serverInfoService.connectAvailable(info);
        return AppResult.ok("Server connection succeeded");
    }


}
