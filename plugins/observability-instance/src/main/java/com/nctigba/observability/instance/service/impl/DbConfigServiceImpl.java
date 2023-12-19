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
 *  DbConfigServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/DbConfigServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.DbConfigService;
import com.nctigba.observability.instance.util.LanguageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DbConfigServiceImpl
 *
 * @since 2023/11/3 10:26
 */
@Service
public class DbConfigServiceImpl implements DbConfigService {
    @Autowired
    private DbConfigMapper dbConfigMapper;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private LanguageUtils language;

    @Override
    @Ds
    public List<Map<String, Object>> cacheHit(String nodeId) {
        return dbConfigMapper.cacheHit();
    }

    @Override
    @Ds
    public Map<String, Object> tablespaceData(String nodeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("tablespaceInfo", dbConfigMapper.tablespaceInfo());
        result.put("tablesTop10", dbConfigMapper.tablesTop10());
        result.put("indexsTop10", dbConfigMapper.indexsTop10());
        result.put("deadTableTop10", dbConfigMapper.deadTableTop10());
        result.put("vacuumTop10", dbConfigMapper.vacuumTop10());
        return result;
    }

    @Override
    @Ds
    public Map<String, Object> getMemoryDetail(String nodeId) {
        Map<String, Object> result = new HashMap<>();
        // memory node detail
        List<Map<String, Object>> memoryNodeDetail = dbConfigMapper.memoryNodeDetail();
        memoryNodeDetail.forEach(map -> {
            var str = map.get("memorytype").toString();
            map.put("desc", messageSource.getMessage("memory.node." + str, null, str, language.getLocale()));
        });
        result.put("memoryNodeDetail", memoryNodeDetail);
        // memory config detail
        List<Map<String, Object>> memoryConfig = dbConfigMapper.memoryConfig();
        memoryConfig.forEach(map -> {
            var str = map.get("name").toString();
            map.put("desc", messageSource.getMessage("memory.config." + str, null, str, language.getLocale()));
        });
        result.put("memoryConfig", memoryConfig);
        return result;
    }
}
