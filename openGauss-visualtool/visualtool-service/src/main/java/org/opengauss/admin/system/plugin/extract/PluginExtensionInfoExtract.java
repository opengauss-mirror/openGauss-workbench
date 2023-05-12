/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 */

package org.opengauss.admin.system.plugin.extract;

import org.opengauss.admin.system.plugin.beans.PluginExtensionInfoDto;

/**
 * PluginExtensionInfoExtract
 *
 * @author YanHuan
 * @since 2023-04-26
 */
public interface PluginExtensionInfoExtract {
    /**
     * Get Plugin Extension Info
     *
     * @return 返回插件扩展信息
     */
    PluginExtensionInfoDto getPluginExtensionInfo();
}
