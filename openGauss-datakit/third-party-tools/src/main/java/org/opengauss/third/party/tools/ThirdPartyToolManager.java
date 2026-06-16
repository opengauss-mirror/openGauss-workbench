/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools;

import org.opengauss.third.party.tools.entity.BaseInstallInfo;
import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.opengauss.third.party.tools.repository.InstallInfoRepository;
import org.opengauss.third.party.tools.repository.ThirdPartyToolRepository;
import org.opengauss.third.party.tools.repository.impl.InstallInfoCsvRepository;
import org.opengauss.third.party.tools.repository.impl.ThirdPartyToolCsvRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Third party tool manager class
 *
 * @since 2026/6/8
 */
public class ThirdPartyToolManager {
    private static final InstallInfoRepository INSTALL_INFO_REPOSITORY = new InstallInfoCsvRepository();
    private static final ThirdPartyToolRepository THIRD_PARTY_TOOL_REPOSITORY = new ThirdPartyToolCsvRepository();

    private ThirdPartyToolManager() {
    }

    /**
     * Save third party tool install info
     *
     * @param thirdPartyToolEnum third party tool enum
     * @param installInfo        install info
     * @throws IOException io exception
     */
    public static void save(ThirdPartyToolEnum thirdPartyToolEnum, BaseInstallInfo installInfo)
            throws IOException {
        if (thirdPartyToolEnum == null || installInfo == null) {
            throw new IllegalArgumentException("Argument 'thirdPartyToolEnum' or 'installInfo' can not be null");
        }
        if (!installInfo.getClass().getName().equals(thirdPartyToolEnum.getInstallInfoClassName())) {
            throw new IllegalArgumentException(
                    "The class of 'installInfo' argument does not match the 'thirdPartyToolEnum' argument");
        }

        synchronized (thirdPartyToolEnum) {
            if (!Files.exists(Paths.get(thirdPartyToolEnum.getInstallInfoCsvFilePath()))) {
                THIRD_PARTY_TOOL_REPOSITORY.register(thirdPartyToolEnum);
            }
            INSTALL_INFO_REPOSITORY.deleteById(thirdPartyToolEnum, installInfo.getId());
            INSTALL_INFO_REPOSITORY.save(thirdPartyToolEnum, installInfo);
        }
    }

    /**
     * Delete third party tool install info by id
     *
     * @param thirdPartyToolEnum third party tool enum
     * @param id                 install info id
     * @throws IOException io exception
     */
    public static void deleteById(ThirdPartyToolEnum thirdPartyToolEnum, String id) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Argument 'id' can not be null or blank");
        }
        if (thirdPartyToolEnum == null) {
            throw new IllegalArgumentException("Argument 'thirdPartyToolEnum' can not be null");
        }

        synchronized (thirdPartyToolEnum) {
            INSTALL_INFO_REPOSITORY.deleteById(thirdPartyToolEnum, id);
        }
    }
}
