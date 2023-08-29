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
 * IOpsHostTagRelService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsHostTagRelService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostTagRel;
import org.opengauss.admin.common.core.domain.model.ops.host.tag.HostTagInputDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lhf
 * @date 2023/3/14 23:38
 **/
public interface IOpsHostTagRelService extends IService<OpsHostTagRel> {
    void addHostTagRel(List<String> hostIds, List<OpsHostTagEntity> tags);

    Map<String, Set<String>> mapByHostIds(List<String> hostIds);

    void cleanHostTag(String hostId);

    void delByTagId(String tagId);

    void delTagRelation(HostTagInputDto hostTagInputDto);
}
