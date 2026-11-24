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
public class Bmap {

    private String key;
    private List<Double> center;
    private int zoom;
    private boolean roam;
    private MapStyle mapStyle;

    public Bmap setKey(String key) {
        this.key = key;
        return this;
    }
    public String getKey() {
        return key;
    }

    public Bmap setCenter(List<Double> center) {
        this.center = center;
        return this;
    }
    public List<Double> getCenter() {
        return center;
    }

    public Bmap setZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }
    public int getZoom() {
        return zoom;
    }

    public Bmap setRoam(boolean roam) {
        this.roam = roam;
        return this;
    }
    public boolean getRoam() {
        return roam;
    }

    public Bmap setMapStyle(MapStyle mapStyle) {
        this.mapStyle = mapStyle;
        return this;
    }
    public MapStyle getMapStyle() {
        return mapStyle;
    }

}
