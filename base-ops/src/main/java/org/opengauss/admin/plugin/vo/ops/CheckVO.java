package org.opengauss.admin.plugin.vo.ops;

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
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }

                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail");
            }
        }

        Field[] dbField = db.getClass().getDeclaredFields();
        for (Field declaredField : dbField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(db);
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }

                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail");
            }
        }

        Field[] osField = os.getClass().getDeclaredFields();
        for (Field declaredField : osField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(os);
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }

                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail");
            }
        }

        Field[] deviceField = device.getClass().getDeclaredFields();
        for (Field declaredField : deviceField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(device);
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }

                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail");
            }
        }

        Field[] networkField = network.getClass().getDeclaredFields();
        for (Field declaredField : networkField) {
            declaredField.setAccessible(true);
            try {
                CheckItemVO checkItemVO = (CheckItemVO) declaredField.get(network);
                String status = checkItemVO.getStatus();
                List<CheckItemVO> checkItemVOS = summary.get(status);
                if (Objects.isNull(checkItemVOS)) {
                    checkItemVOS = new ArrayList<>();
                    summary.put(status, checkItemVOS);
                }

                checkItemVOS.add(checkItemVO);
            } catch (Exception e) {
                log.error("get fail");
            }
        }

        return summary;
    }
}
