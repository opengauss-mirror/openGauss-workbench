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
 * MainTaskEnvErrorHostService.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/MainTaskEnvErrorHostService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MainTaskEnvErrorHost;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MainTaskEnvErrorHostService extends IService<MainTaskEnvErrorHost> {


    void saveRecord(Integer mainTaskId, MigrationTaskHostRef hostRef);

    void deleteByMainTaskId(Integer mainTaskId);

    MainTaskEnvErrorHost getOneByMainTaskId(Integer mainTaskId);
}
