/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.data;

import lombok.Getter;

import org.opengauss.admin.common.core.domain.model.agent.AgentClusterVo;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * <pre>
 * ClusterOriginal enum  implements TaskClusterConfigLoader interface,
 * The original cluster information is obtained from the database, loading by : ops_cluster, ops_jdbc_cluster.
 * </pre>
 *
 * @author: wangchao
 * @Date: 2025/3/18 16:17
 * @Description: ClusterOriginal
 * @since 7.0.0-RC2
 **/
@Getter
public enum ClusterOriginal implements TaskClusterConfigLoader {
    /**
     * ops_cluster: load cluster information from ops_cluster table.
     */
    OPS_CLUSTER("ops_cluster") {
        final String sql =
            "select c.cluster_id clusterId,n.cluster_node_id clusterNodeId,'OPENGAUSS' dataBaseType,h.public_ip hostIp,"
                + " c.port port, c.database_username username,c.database_password dbPassword "
                + " from ops_cluster c left join ops_cluster_node n on c.cluster_id=n.cluster_id "
                + " left join ops_host h on n.host_id=h.host_id where n.cluster_node_id=?";

        @Override
        public AgentClusterVo load(String clusterNodeId) {
            BeanPropertyRowMapper<AgentClusterVo> rowMapper = new BeanPropertyRowMapper<>(AgentClusterVo.class);
            return jdbcTemplate.queryForObject(sql, rowMapper, clusterNodeId);
        }
    },
    /**
     * ops_jdbc_cluster: load cluster information from ops_jdbcdb_cluster table.
     */
    OPS_JDBC_CLUSTER("ops_jdbc_cluster") {
        final String sql =
            "select c.cluster_id clusterId,n.cluster_node_id clusterNodeId ,c.db_type dataBaseType,n.ip hostIp,"
                + " n.port port, n.username username,n.password dbPassword,n.url url "
                + " from ops_jdbcdb_cluster c left join ops_jdbcdb_cluster_node n on c.cluster_id= n.cluster_id "
                + " where n.cluster_node_id=?;";

        @Override
        public AgentClusterVo load(String clusterNodeId) {
            BeanPropertyRowMapper<AgentClusterVo> rowMapper = new BeanPropertyRowMapper<>(AgentClusterVo.class);
            return jdbcTemplate.queryForObject(sql, rowMapper, clusterNodeId);
        }
    };

    private final String value;
    private static JdbcTemplate jdbcTemplate;

    /**
     * constructor
     *
     * @param value cluster type
     */
    ClusterOriginal(String value) {
        this.value = value;
    }

    @Component
    static class ClusterServiceHolder {
        /**
         * Set jdbcTemplate
         *
         * @param jdbcTemplate JdbcTemplate
         */
        @Resource
        public void setMappers(JdbcTemplate jdbcTemplate) {
            ClusterOriginal.jdbcTemplate = jdbcTemplate;
        }
    }
}
