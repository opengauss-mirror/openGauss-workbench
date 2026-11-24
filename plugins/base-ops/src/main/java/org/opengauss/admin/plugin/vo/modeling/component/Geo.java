/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.vo.modeling.component;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Geo {

    private String map;
    private List<Double> center;
    private Double zoom;
    private boolean roam;

    public Geo setMap(String map) {
        this.map = map;
        return this;
    }
    public String getMap() {
        return map;
    }

    public Geo setCenter(List<Double> center) {
        this.center = center;
        return this;
    }
    public List<Double> getCenter() {
        return center;
    }

    public Geo setZoom(Double zoom) {
        this.zoom = zoom;
        return this;
    }
    public Double getZoom() {
        return zoom;
    }

    public Geo setRoam(boolean roam) {
        this.roam = roam;
        return this;
    }
    public boolean getRoam() {
        return roam;
    }

}
