/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * HostTagInputDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/host/tag/HostTagInputDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.host.tag;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author lhf
 * @date 2023/3/14 23:45
 **/
@Data
public class HostTagInputDto {
    private List<String> names;
    private List<String> hostIds;

    public static HostTagInputDto of(List<String> tags, String hostId) {
        HostTagInputDto hostTagInputDto = new HostTagInputDto();
        hostTagInputDto.setNames(tags);
        hostTagInputDto.setHostIds(Arrays.asList(hostId));
        return hostTagInputDto;
    }
}
