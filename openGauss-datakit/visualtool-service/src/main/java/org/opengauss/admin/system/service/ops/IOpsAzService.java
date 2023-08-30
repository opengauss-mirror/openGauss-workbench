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
 * IOpsAzService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsAzService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;

/**
 * O&M Availability Zone Service Class
 *
 * @author lhf
 * @date 2022/8/6 20:49
 **/
public interface IOpsAzService extends IService<OpsAzEntity> {
    /**
     * Add Availability Zone
     *
     * @param az
     * @return
     */
    boolean add(OpsAzEntity az);

    boolean hasName(String name);
}
