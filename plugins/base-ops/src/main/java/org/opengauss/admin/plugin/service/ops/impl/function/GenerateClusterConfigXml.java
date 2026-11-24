/*
 * Copyright (c) 2024 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import org.opengauss.admin.plugin.domain.model.ops.InstallContext;

/**
 * GenerateClusterConfigXml
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@FunctionalInterface
public interface GenerateClusterConfigXml {
    /**
     * generate openGauss enterprise cluster config xml
     *
     * @param installContext install context
     * @return xml
     */
    String generate(InstallContext installContext);
}
