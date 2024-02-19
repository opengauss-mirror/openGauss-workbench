/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDisasterClusterEntity;

import java.util.List;
import java.util.Map;

/**
 * disaster cluster mapper
 *
 * @author wbd
 * @since 2024/1/26 09:04
 **/
@Mapper
public interface OpsDisasterClusterMapper extends BaseMapper<OpsDisasterClusterEntity> {
    /**
     * query count of disaster cluster by device manager name
     *
     * @param deviceManagerName deviceManagerName
     * @return int
     */
    int queryDisasterClusterCountByDeviceManagerName(@Param("deviceManagerName") String deviceManagerName);

    /**
     * query count of disaster cluster by cluster name
     *
     * @param clusterName clusterName
     * @return int
     */
    int queryDisasterClusterCountByClusterName(@Param("clusterName") String clusterName);

    /**
     * query disaster cluster info
     *
     * @return List
     */
    List<Map<String, String>> queryDisasterClusterInfo();
}
