/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.repository;

import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.opengauss.third.party.tools.entity.BaseInstallInfo;

import java.io.IOException;

/**
 * Install info repository interface
 *
 * @since 2026/6/8
 */
public interface InstallInfoRepository {
    /**
     * Save install info to repository
     *
     * @param toolEnum    third party tool enum
     * @param installInfo install info
     * @throws IOException io exception
     */
    void save(ThirdPartyToolEnum toolEnum, BaseInstallInfo installInfo) throws IOException;

    /**
     * Delete install info by id
     *
     * @param toolEnum third party tool enum
     * @param id       install info id
     * @throws IOException io exception
     */
    void deleteById(ThirdPartyToolEnum toolEnum, String id) throws IOException;
}
