/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
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
