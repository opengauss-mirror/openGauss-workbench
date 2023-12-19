/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ParamValueInfoServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/ParamValueInfoServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.instance.model.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.model.entity.ParamInfoDO;
import com.nctigba.observability.instance.model.entity.ParamValueInfoDO;
import com.nctigba.observability.instance.mapper.ParamValueInfoMapper;
import com.nctigba.observability.instance.service.ParamValueInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ParamValueInfoServiceImpl
 *
 * @author liupengfei
 * @since 2023/12/1
 */
@Service
public class ParamValueInfoServiceImpl extends ServiceImpl<ParamValueInfoMapper, ParamValueInfoDO>
        implements ParamValueInfoService {
    @Autowired
    ParamInfoServiceImpl paramInfoService;

    /**
     * Cacheable query
     *
     * @param nodeId String
     * @return List<ParamInfoDTO>
     */
    @Cacheable(cacheNames = "paraminfo", key = "#nodeId")
    public List<ParamInfoDTO> query(String nodeId) {
        var result = new ArrayList<ParamInfoDTO>();
        List<ParamValueInfoDO> list = list(
                Wrappers.lambdaQuery(ParamValueInfoDO.class).eq(ParamValueInfoDO::getInstance, nodeId));
        var map = new HashMap<Integer, ParamValueInfoDO>();
        for (ParamValueInfoDO paramValueInfoDO : list) {
            map.put(paramValueInfoDO.getSid(), paramValueInfoDO);
        }
        for (ParamInfoDO info : paramInfoService.getAll()) {
            String act;
            if (map.containsKey(info.getId())) {
                act = map.get(info.getId()).getActualValue();
            } else {
                act = "";
            }
            result.add(new ParamInfoDTO(info, act));
        }
        return result;
    }

    /**
     * CacheEvict refresh
     *
     * @param nodeId String
     */
    @CacheEvict(cacheNames = "paraminfo", key = "#nodeId")
    public void refresh(String nodeId) {
        // Do nothing because of X and Y.
    }
}
