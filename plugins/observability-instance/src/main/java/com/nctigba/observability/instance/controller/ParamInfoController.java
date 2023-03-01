package com.nctigba.observability.instance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.dto.param.DatabaseParamDTO;
import com.nctigba.observability.instance.dto.param.OsParamDTO;
import com.nctigba.observability.instance.entity.NctigbaParamInfo;
import com.nctigba.observability.instance.mapper.NctigbaParamInfoMapper;
import com.nctigba.observability.instance.model.param.ParamQuery;
import com.nctigba.observability.instance.service.ParamInfoService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
    private NctigbaParamInfoMapper mapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected EncryptionUtils encryptionUtils;

    /*@ApiOperation("Database Param Info")
    @GetMapping(value = "/databaseParamInfo")
    public List<DatabaseParamDTO> databaseParamInfo(ParamQuery paramQuery) {
        return paramInfoService.getDatabaseParamInfo(paramQuery);
    }

    @ApiOperation("Os Param Info")
    @GetMapping(value = "/osParamInfo")
    public List<OsParamDTO> osParamInfo(ParamQuery paramQuery) {
        if(paramQuery.getPassword()!=null && !"".equals(paramQuery.getPassword())){
            String password=encryptionUtils.decrypt(paramQuery.getPassword());
            paramQuery.setPassword(password);
        }
        return paramInfoService.getOsParamInfo(paramQuery);
    }*/
    @ApiOperation("Param Info")
    @GetMapping(value = "/paramInfo")
    public List<NctigbaParamInfo> databaseParamInfo(ParamQuery paramQuery) {
        if("1".equals(paramQuery.getIsRefresh())){
            /*if(paramQuery.getPassword()!=null && !"".equals(paramQuery.getPassword())){
                String password=encryptionUtils.decrypt(paramQuery.getPassword());
                paramQuery.setPassword(password);
            }*/
            return paramInfoService.getParamInfo(paramQuery);
        }else{
            return mapper.selectList(Wrappers.emptyWrapper());
        }
    }
}
