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
 *  ElasticSearchInstallDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/dto/ElasticSearchInstallDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.dto;

import lombok.Data;

/**
 * ElasticSearchInstallDTO
 *
 * @author luomeng
 * @since 2024/2/20
 */
@Data
public class ElasticSearchInstallDTO {
    String hostId;
    String path;
    String username;
    Integer port;
}
