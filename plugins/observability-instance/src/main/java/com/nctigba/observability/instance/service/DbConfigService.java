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
 *  DbConfigService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/DbConfigService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import java.util.List;
import java.util.Map;

/**
 * DbConfigService
 *
 * @since 2023/11/3 10:22
 */
public interface DbConfigService {
    /**
     * cache hit rate
     *
     * @param nodeId cluster node Id
     * @return List<Map<String, Object>>
     */
    List<Map<String, Object>> cacheHit(String nodeId);

    /**
     * tablespaceData
     *
     * @param nodeId cluster node Id
     * @return List<Map<String, Object>>
     */
    Map<String, Object> tablespaceData(String nodeId);

    /**
     * getMemoryDetail
     *
     * @param nodeId String
     * @return Map<String, Object>
     */
    Map<String, Object> getMemoryDetail(String nodeId);
}
