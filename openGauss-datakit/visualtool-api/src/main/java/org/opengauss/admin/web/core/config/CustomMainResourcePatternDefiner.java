/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.web.core.config;

import com.gitee.starblues.core.classloader.MainResourcePatternDefiner;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * CustomMainResourcePatternDefiner
 *
 * @author: wangchao
 * @Date: 2025/9/18 17:20
 * @since 7.0.0-RC2
 **/
@Component
public class CustomMainResourcePatternDefiner implements MainResourcePatternDefiner {
    @Override
    public Set<String> getIncludePatterns() {
        // 包含包匹配
        Set<String> includePatterns = new HashSet<>();
        includePatterns.add("jakarta/websocket/**");
        includePatterns.add("jakarta/servlet/**");
        includePatterns.add("jakarta/annotation/**");
        return includePatterns;
    }

    @Override
    public Set<String> getExcludePatterns() {
        return new HashSet<>();
    }
}
