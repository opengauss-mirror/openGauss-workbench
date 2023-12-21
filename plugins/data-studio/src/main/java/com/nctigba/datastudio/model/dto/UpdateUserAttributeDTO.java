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
 *  UpdateUserAttributeDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/UpdateUserAttributeDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * UpdateUserAttributeDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class UpdateUserAttributeDTO {
    private String uuid;
    private String webUser;
    private String oldName;
    private String newName;
    private String beginDate;
    private String endDate;
    private String connectionLimit;
    private String resourcePool;
    private Map<String, Boolean> changePower;
    private Map<String, Boolean> changeBelong;
    private String comment;
    private String type;
}
