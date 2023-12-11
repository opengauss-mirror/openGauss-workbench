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
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service;

import java.util.List;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.utils.response.RespBean;

/**
 * 功能描述
 *
 * @author liu
 * @since 2022-10-01
 */
public interface LinuxConfigService {
    /**
     * getList
     *
     * @param config config
     * @return RespBean RespBean
     */
    RespBean getLinuxConfigList(LinuxConfig config);

    /**
     * saveLinuxConfig
     *
     * @param config config
     * @return RespBean RespBean
     */
    RespBean saveLinuxConfig(LinuxConfig config);

    /**
     * updateLinuxConfig
     *
     * @param config config
     * @return RespBean RespBean
     */
    RespBean updateLinuxConfig(LinuxConfig config);

    /**
     * deleteLinuxConfig
     *
     * @param ids ids
     * @return RespBean RespBean
     */
    RespBean deleteLinuxConfig(List<Integer> ids);

    /**
     * getHostList
     *
     * @return RespBean RespBean
     */
    RespBean getHostList();
}
