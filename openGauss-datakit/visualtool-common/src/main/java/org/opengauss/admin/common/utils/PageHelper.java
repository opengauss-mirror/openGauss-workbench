/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * PageHelper
 *
 * @since 2025/11/7
 */
public class PageHelper {
    /**
     * Get page from list
     *
     * @param list list
     * @param page page
     * @param <T>  type
     * @return page
     */
    public static <T> Page<T> getPageFromList(List<T> list, Page<?> page) {
        long current = page == null || page.getCurrent() <= 0 ? 1 : page.getCurrent();
        long size = page == null || page.getSize() <= 0 ? 10 : page.getSize();
        int total = list == null ? 0 : list.size();

        Page<T> resPage = new Page<>(current, size, total);
        if (total == 0) {
            return resPage;
        }

        long totalPages = (total + size - 1) / size;
        if (current > totalPages) {
            resPage.setRecords(new ArrayList<>());
        } else {
            int startIndex = (int) ((current - 1) * size);
            int endIndex = (int) Math.min(startIndex + size, total);
            resPage.setRecords(new ArrayList<>(list.subList(startIndex, endIndex)));
        }

        return resPage;
    }
}
