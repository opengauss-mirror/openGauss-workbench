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
 * IOpsWdrService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsWdrService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsWdrEntity;
import org.opengauss.admin.plugin.domain.model.ops.WdrGeneratorBody;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lhf
 * @date 2022/10/13 15:13
 **/
public interface IOpsWdrService extends IService<OpsWdrEntity> {
    List<OpsWdrEntity> listWdr(String clusterId, WdrScopeEnum wdrScope, WdrTypeEnum wdrType, String hostId, Date start, Date end);

    void generate(WdrGeneratorBody wdrGeneratorBody);

    Page<DwrSnapshotVO> listSnapshot(Page page, String clusterId, String hostId);

    void createSnapshot(String clusterId, String hostId);

    void del(String id);

    void downloadWdr(String wdrId, HttpServletResponse response);
}
