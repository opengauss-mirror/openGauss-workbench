/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.base;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;

@Service
@Slf4j
public class ClusterManager {
    @Autowired(required = false)
    @AutowiredType(Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;

    public List<OpsClusterVO> getAllOpsCluster() {
        List<OpsClusterVO> opsClusterVOList = new ArrayList<>();
        try {
            if (opsFacade != null) {
                opsClusterVOList = opsFacade.listCluster();
            }
        } catch (Exception e) {
            log.info("get all ops cluster fail:{}", e.getMessage());
        }
        return opsClusterVOList;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
        private String version;

        public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
            BeanUtils.copyProperties(opsClusterNodeVO, this);
            this.version = version;
        }

        public Connection connection() throws SQLException {
            return DriverManager.getConnection(
                    GET_URL_JDBC + getPrivateIp() + ":" + getDbPort() + "/" + getDbName() + CONFIGURE_TIME,
                    getDbUser(),
                    getDbUserPassword());
        }
    }
}