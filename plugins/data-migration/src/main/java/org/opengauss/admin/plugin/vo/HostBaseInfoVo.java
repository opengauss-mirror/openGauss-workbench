/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Host base info vo
 *
 * @since 2025/7/22
 */
@Data
public class HostBaseInfoVo implements Comparable<HostBaseInfoVo> {
    private String cpuCoreNum;
    private String cpuUsing;
    private String remainingMemory;
    private String availableDiskSpace;

    /**
     * Is empty
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return cpuCoreNum == null && cpuUsing == null && remainingMemory == null && availableDiskSpace == null;
    }

    @Override
    public int compareTo(@NotNull HostBaseInfoVo o) {
        int cpuCoreNumCompare = Integer.compare(Integer.parseInt(o.getCpuCoreNum()), Integer.parseInt(getCpuCoreNum()));
        if (cpuCoreNumCompare != 0) {
            return cpuCoreNumCompare;
        }

        int cpuUsingCompare = Float.compare(Float.parseFloat(getCpuUsing()), Float.parseFloat(o.getCpuUsing()));
        if (cpuUsingCompare != 0) {
            return cpuUsingCompare;
        }

        int remainingMemoryCompare = Double.compare(
                Double.parseDouble(o.getRemainingMemory()), Double.parseDouble(getRemainingMemory()));
        if (remainingMemoryCompare != 0) {
            return remainingMemoryCompare;
        }

        return Float.compare(
                Float.parseFloat(o.getAvailableDiskSpace()), Float.parseFloat(getAvailableDiskSpace()));
    }
}
