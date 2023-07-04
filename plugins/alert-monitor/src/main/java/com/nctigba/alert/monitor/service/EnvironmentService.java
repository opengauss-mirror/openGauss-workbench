/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import com.nctigba.alert.monitor.config.annotation.AlertContentParam;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuyuebin
 * @date 2023/6/16 15:42
 * @description
 */
@Service
public class EnvironmentService {
    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;

    public List cluster() {
        return opsFacade.listCluster();
    }

    public void checkPrometheus() {
        List<NctigbaEnv> env = envMapper
                .selectList(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.Type.PROMETHEUS));
        if (CollectionUtil.isEmpty(env)) {
            throw new ServiceException("the promethues is uninstall");
        }
    }

    public Map<String, Map<String, String>> getAlertContentParam(String type) {
        MessageSourceUtil.reset();
        Map<String, Map<String, String>> map = new HashMap<>();
        Class<AlertContentParamDto> clazz = AlertContentParamDto.class;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(AlertContentParam.class)) {
                Map<String, String> param = new HashMap<>();
                AlertContentParam annotation = field.getAnnotation(AlertContentParam.class);
                List<String> groups = Arrays.asList(annotation.group());
                if (!groups.contains(type)) {
                    continue;
                }
                boolean isI18nPreVal = annotation.isI18nPreVal();
                param.put("name", MessageSourceUtil.get(annotation.name()));
                param.put("preVal", isI18nPreVal ? MessageSourceUtil.get(annotation.preVal()) : annotation.preVal());
                map.put(field.getName(), param);
            }
        }
        return map;
    }
}
