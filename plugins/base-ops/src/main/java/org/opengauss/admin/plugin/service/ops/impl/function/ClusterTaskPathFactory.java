/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * ClusterTaskPathFactory.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/function/ClusterTaskPathFactory.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ClusterTaskPathFactory
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Service
public class ClusterTaskPathFactory {
    private ClusterTaskPathFunction taskPathFunction = path -> {
        if (StrUtil.isNotEmpty(path)) {
            String[] split = path.split("/");
            if (split.length > 1) {
                return "/" + split[1];
            }
        }
        return "";
    };

    /**
     * Add path to pathMap
     *
     * @param pathMap pathMap
     * @param path    path
     */
    public void addPath(Map<String, String> pathMap, String path) {
        if (StrUtil.isNotEmpty(path)) {
            String apply = taskPathFunction.apply(path);
            pathMap.put(apply, "");
        }
    }

    /**
     * Apply path ,get the path after applying the function
     *
     * @param path path
     * @return top path
     */
    public String applyPath(String path) {
        return taskPathFunction.apply(path);
    }

    /**
     * Add all paths to pathMap
     *
     * @param pathMap pathMap
     * @param paths   paths
     */
    public void addAllPath(Map<String, String> pathMap, List<String> paths) {
        paths.forEach(path -> {
            addPath(pathMap, path);
        });
    }

    /**
     * ClusterTaskPathFunction
     */
    @FunctionalInterface
    public interface ClusterTaskPathFunction {
        /**
         * apply path function
         *
         * @param path path
         * @return top path
         */
        String apply(String path);
    }
}
