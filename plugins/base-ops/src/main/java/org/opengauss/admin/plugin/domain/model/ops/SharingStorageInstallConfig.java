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
 * SharingStorageInstallConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/SharingStorageInstallConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.enums.ops.ConnectTypeEnum;

import java.util.List;
import java.util.Objects;

/**
 * sharing storage install config
 *
 * @author shenzheng
 * @since 2023.10.19
 **/
@Slf4j
@Data
public class SharingStorageInstallConfig {
    /**
     * whether dss is on
     */
    private boolean isEnableDss = false;

    private String clusterType = "single-inst";

    /**
     * dss instance home
     */
    private String dssHome;

    /**
     * dss volume name
     */
    private String dssVgName = "data";

    /**
     * Lun for data, all nodes sharing one Lun
     */
    private String dssDataLunPath;

    /**
     * Lun for data, using ln -s
     */
    private String dssDataLunLinkPath;

    /**
     * Vg name of xlog LUN
     */
    private String xlogVgName = "xlog";

    /**
     * Lun for xlog
     */
    private List<String> xlogLunPath;

    /**
     * Lun for xlog, using ln -s
     */
    private List<String> xlogLunLinkPath;

    /**
     * CM sharing Lun
     */
    private String cmSharingLunPath;

    /**
     * CM sharing Lun, using ln -s
     */
    private String cmSharingLunLinkPath;

    /**
     * CM voting Lun
     */
    private String cmVotingLunPath;

    /**
     * CM voting Lun, using ln -s
     */
    private String cmVotingLunLinkPath;

    /**
     * whether enable ssl when dss is on
     */
    private boolean enableSsl = true;

    /**
     * inter mes connect type
     */
    private ConnectTypeEnum interconnectType = ConnectTypeEnum.TCP;

    /**
     * RDMA config
     */
    private String rdamConfig;

    /**
     * RDMA config
     */
    private String rdamLogPath;

    private String getLinkPath(String lunPath) {
        LunPathManager lunPathManager = new LunPathManager(lunPath);
        String[] s = lunPath.split(" ");
        if (s.length < 3) {
            throw new OpsException("invalid lun path :" + lunPath);
        }

        if (lunPathManager.getWwn().length() < LunPathManager.WWN_LEN_IN_HEX) {
            throw new OpsException("invalid wwn :" + lunPathManager.getWwn());
        }

        String lastPart = lunPathManager.getWwn().substring(lunPathManager.getWwn().length()
                - LunPathManager.SOFT_LINK_WWN_LEN);
        return "/dev/ln_" + lastPart;
    }

    /**
     * check configuration of sharing storage
     */
    public void checkConfig() {
        log.info("Start checking sharing storage configuration.");
        if (!isEnableDss) {
            throw new OpsException("isEnableDss is off when sharing storage arch is chose.");
        }

        if (StrUtil.isEmpty(dssHome)) {
            throw new OpsException("Dss home is empty.");
        }

        if (StrUtil.isEmpty(dssVgName)) {
            throw new OpsException("Dss vg name is empty.");
        }

        if (StrUtil.isEmpty(dssDataLunPath)) {
            throw new OpsException("Dss data lun is empty.");
        }

        if (StrUtil.isEmpty(xlogVgName)) {
            throw new OpsException("Dss xlog vg name is empty.");
        }

        if (xlogLunPath.isEmpty()) {
            throw new OpsException("Dss xlog lun is empty.");
        }

        if (StrUtil.isEmpty(cmSharingLunPath)) {
            throw new OpsException("CM sharing lun is empty.");
        }

        if (StrUtil.isEmpty(cmVotingLunPath)) {
            throw new OpsException("CM voting lun is empty.");
        }

        if (Objects.isNull(interconnectType)) {
            throw new OpsException("inter connect type is empty.");
        }

        if (interconnectType == ConnectTypeEnum.RDMA) {
            if (StrUtil.isEmpty(rdamConfig)) {
                throw new OpsException("when interconnectType = RDMA, rdmaConfig can not be empty.");
            }

            if (StrUtil.isEmpty(rdamLogPath)) {
                throw new OpsException("when interconnectType = RDMA, rdmaLogPath can not be empty.");
            }
        }
        log.info("Checking sharing storage configuration success.");
    }

    /**
     * append sharing storage to cluster node entity
     *
     * @param opsClusterNodeEntity OpsClusterNodeEntity
     */
    public void appendtoOpsClusterNodeEntity(OpsClusterNodeEntity opsClusterNodeEntity) {
        if (isEnableDss) {
            opsClusterNodeEntity.setIsEnableDss(true);
            opsClusterNodeEntity.setDssDataLunLinkPath(dssDataLunLinkPath);
            String output = StringUtils.join(xlogLunLinkPath, ",");
            opsClusterNodeEntity.setXlogLunLinkPath(output);
            opsClusterNodeEntity.setCmVotingLunLinkPath(cmVotingLunLinkPath);
            opsClusterNodeEntity.setCmSharingLunLinkPath(cmSharingLunLinkPath);
        } else {
            opsClusterNodeEntity.setIsEnableDss(false);
            opsClusterNodeEntity.setDssDataLunLinkPath("");
            opsClusterNodeEntity.setXlogLunLinkPath("");
            opsClusterNodeEntity.setCmVotingLunLinkPath("");
            opsClusterNodeEntity.setCmSharingLunLinkPath("");
        }
    }
}
