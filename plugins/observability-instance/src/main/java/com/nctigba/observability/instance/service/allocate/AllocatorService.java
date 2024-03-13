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
 *  AllocatorService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/allocate/AllocatorService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.allocate;

import com.nctigba.observability.instance.model.dto.AllocateServerDTO;

import java.util.List;
import java.util.Map;

/**
 * IAllocator
 *
 * @since 2024/3/8 17:23
 */
public interface AllocatorService {
    void init(List<AllocateServerDTO> allocators);
    String getAllocatorId(AllocateServerDTO recipient);
    Map<String, List<String>> alloc(List<AllocateServerDTO> recipients);
}
