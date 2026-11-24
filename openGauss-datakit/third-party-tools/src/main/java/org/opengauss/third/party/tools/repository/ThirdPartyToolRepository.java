/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.third.party.tools.repository;

import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;

import java.io.IOException;

/**
 * Third party tool repository interface
 *
 * @since 2026/6/8
 */
public interface ThirdPartyToolRepository {
    /**
     * Register third party tool
     *
     * @param thirdPartyToolEnum third party tool enum
     * @throws IOException io exception
     */
    void register(ThirdPartyToolEnum thirdPartyToolEnum) throws IOException;
}
