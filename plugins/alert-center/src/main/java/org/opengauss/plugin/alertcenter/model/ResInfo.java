/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResInfo {
    private int exitStatus;
    private String outRes;
    private String errRes;

    public ResInfo() {
        this.exitStatus = -2147483648;
    }
}
