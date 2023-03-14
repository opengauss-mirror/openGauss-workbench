package com.nctigba.observability.instance.controller;

import java.util.List;

import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.service.ParamInfoService;

import lombok.RequiredArgsConstructor;


/**
 * ParamInfo
 *
 * @author luomeng@ncti-gba.cn
 * @since 2023/01/30 15:00
 */
@RestController
@RequestMapping("/observability/v1/param")
@RequiredArgsConstructor
public class ParamInfoController {

    private final ParamInfoService paramInfoService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected EncryptionUtils encryptionUtils;

    @GetMapping(value = "/paramInfo")
    public List<ParamInfoDTO> paramInfo(ParamQuery paramQuery) {
        if("".equals(paramQuery.getNodeId()) || paramQuery.getNodeId()==null){
            throw new InstanceException("nodeId is empty!");
        }
        if (paramQuery.getPassword() != null && !"".equals(paramQuery.getPassword())) {
            String password = encryptionUtils.decrypt(paramQuery.getPassword());
            paramQuery.setPassword(password);
        }
        List<ParamInfoDTO> list=paramInfoService.getParamInfo(paramQuery);
        if(list==null){
            throw new InstanceException("password is error!");
        }
        return list;
    }
}
