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
 *  ConsistentHashAllocatorServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/allocate/impl/ConsistentHashAllocatorServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.allocate.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.model.dto.AllocateServerDTO;
import com.nctigba.observability.instance.service.allocate.AllocatorService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ConsistentHashAllocator
 *
 * @since 2024/3/8 17:41
 */
@Service
public class ConsistentHashAllocatorServiceImpl implements AllocatorService {
    private ThreadLocal<SortedMap<Integer, AllocateServerDTO>> alloctorMapTL = new ThreadLocal<>();

    @Override
    public void init(List<AllocateServerDTO> allocators) {
        if (CollectionUtil.isEmpty(allocators)) {
            return;
        }
        SortedMap<Integer, AllocateServerDTO> map = new TreeMap<>();
        for (AllocateServerDTO allocator : allocators) {
            String addr = allocator.getIp() + ":" + allocator.getPort();
            Integer hashcode = addr.hashCode();
            map.put(hashcode, allocator);
        }
        alloctorMapTL.set(map);
    }

    @Override
    public String getAllocatorId(AllocateServerDTO recipient) {
        SortedMap<Integer, AllocateServerDTO> alloctorMap = alloctorMapTL.get();
        if (CollectionUtil.isEmpty(alloctorMap)) {
            return "";
        }
        String addr = recipient.getIp() + ":" + recipient.getPort();
        SortedMap<Integer, AllocateServerDTO> subMap = alloctorMap.tailMap(addr.hashCode());
        if(subMap.isEmpty()) {
           return alloctorMap.get(alloctorMap.firstKey()).getId();
        }
        return alloctorMap.get(subMap.firstKey()).getId();
    }

    @Override
    public Map<String, List<String>> alloc(List<AllocateServerDTO> recipients) {
        if (CollectionUtil.isEmpty(recipients)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        SortedMap<Integer, AllocateServerDTO> alloctorMap = alloctorMapTL.get();
        alloctorMap.values().forEach(item -> {
            map.put(item.getId(), new ArrayList<>());
        });
        for (AllocateServerDTO server : recipients) {
            String id = getAllocatorId(server);
            if (StrUtil.isBlank(id)) {
                continue;
            }
            List<String> list = map.get(id);
            if (CollectionUtil.isEmpty(list)) {
                map.put(id, list);
            }
            map.get(id).add(server.getId());
        }
        remove();
        return map;
    }

    /**
     * remove
     */
    @Override
    public void remove() {
        if (CollectionUtil.isNotEmpty(alloctorMapTL.get())) {
            alloctorMapTL.remove();
        }
    }
}
