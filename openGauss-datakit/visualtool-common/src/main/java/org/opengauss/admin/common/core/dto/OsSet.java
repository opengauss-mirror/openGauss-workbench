package org.opengauss.admin.common.core.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.HashSet;

/**
 * used only for Json to Frontend
 *
 * @author chenyuchen2024
 * @date 24/02/22 21:12
 **/
@Data
@AllArgsConstructor
public class OsSet {

    /**
     * os set based on centos
     **/
    private HashSet<String> centosBasedSet;

    /**
     * os set based on openEuler
     **/
    private HashSet<String> openEulerBasedSet;

    /**
     * check whether os is based on centos
     */
    public boolean isCentos(String os) {
        return centosBasedSet.contains(os);
    }

    /**
     * check whether os is based on openEuler
     */
    public boolean isOpenEuler(String os) {
        return openEulerBasedSet.contains(os);
    }
}
