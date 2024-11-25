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
 * CheckVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/check
 * /CheckVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.check;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author lhf
 * @date 2022/10/21 18:34
 **/
@Slf4j
@Data
public class CheckVO {
    /**
     * cluster
     */
    private CheckClusterVO cluster;
    /**
     * database
     */
    private CheckDbVO db;
    /**
     * os
     */
    private CheckOSVO os;
    /**
     * device
     */
    private CheckDeviceVO device;
    /**
     * network
     */
    private CheckNetworkVO network;

    public Map<String, List<CheckItemVO>> summary() {
        Map<String, List<CheckItemVO>> summary = new HashMap<>();
        Field[] clusterField = cluster.getClass().getDeclaredFields();
        for (Field declaredField : clusterField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(cluster);
                if (Objects.isNull(checkItemVO)) {
                    continue;
                }
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }
                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail", e);
            }
        }
        Field[] dbField = db.getClass().getDeclaredFields();
        for (Field declaredField : dbField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(db);
                if (Objects.isNull(checkItemVO)) {
                    continue;
                }
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }
                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail", e);
            }
        }
        Field[] osField = os.getClass().getDeclaredFields();
        for (Field declaredField : osField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(os);
                if (Objects.isNull(checkItemVO)) {
                    continue;
                }
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }
                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail", e);
            }
        }
        Field[] deviceField = device.getClass().getDeclaredFields();
        for (Field declaredField : deviceField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(device);
                if (Objects.isNull(checkItemVO)) {
                    continue;
                }
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }
                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail", e);
            }
        }
        Field[] networkField = network.getClass().getDeclaredFields();
        for (Field declaredField : networkField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(network);
                if (Objects.isNull(checkItemVO)) {
                    continue;
                }
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }
                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail", e);
            }
        }
        return summary;
    }
}
