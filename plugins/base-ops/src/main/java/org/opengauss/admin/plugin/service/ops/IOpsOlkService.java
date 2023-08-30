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
 * IOpsOlkService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsOlkService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkPageVO;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.ShardingRuleDto;

public interface IOpsOlkService extends IService<OpsOlkEntity> {
    void install(InstallOlkBody installBody);

    void remove(String id);

    void destroy(String id, String bid);

    String generateRuleYaml(OlkConfig config);

    void start(String id, String bid);

    void stop(String id, String bid);

    IPage<OlkPageVO> pageOlk(Page page, String name);
}
