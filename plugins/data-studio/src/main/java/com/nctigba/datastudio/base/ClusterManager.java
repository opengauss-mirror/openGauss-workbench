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

/**
 * ClusterManager
 *
 * @since 2023-6-26
 */
@Service
@Slf4j
public class ClusterManager {
    @Autowired(required = false)
    @AutowiredType(Type.MAIN_PLUGIN)
    private OpsFacade opsFacade;

    /**
     * get all ops cluster
     *
     * @return List
     */
    public List<OpsClusterVO> getAllOpsCluster() {
        List<OpsClusterVO> opsClusterVOList = new ArrayList<>();
        if (opsFacade != null) {
            opsClusterVOList = opsFacade.listCluster();
        }
        return opsClusterVOList;
    }

    /**
     * cluster node class
     *
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class OpsClusterNodeVOSub extends OpsClusterNodeVO {
        private String version;

        /**
         * copy cluster node vo
         *
         * @param opsClusterNodeVO opsClusterNodeVO
         * @param version version
         */
        public OpsClusterNodeVOSub(OpsClusterNodeVO opsClusterNodeVO, String version) {
            BeanUtils.copyProperties(opsClusterNodeVO, this);
            this.version = version;
        }

        /**
         * get connection
         *
         * @return Connection
         * @throws SQLException SQLException
         */
        public Connection connection() throws SQLException {
            return DriverManager.getConnection(
                    GET_URL_JDBC + getPrivateIp() + ":" + getDbPort() + "/" + getDbName() + CONFIGURE_TIME,
                    getDbUser(),
                    getDbUserPassword());
        }
    }
}