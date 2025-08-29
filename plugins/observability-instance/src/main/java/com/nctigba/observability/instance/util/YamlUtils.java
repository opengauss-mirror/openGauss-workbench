/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  YamlUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/YamlUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * YamlUtils
 *
 * @author: wangchao
 * @Date: 2025/4/26 17:32
 * @Description: YamlUtils
 * @since 7.0.0-RC2
 **/
public class YamlUtils {
    /**
     * get yaml object
     *
     * @return yaml object
     */
    public static Yaml get() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(new Representer(options) {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                Tag customTag) {
                return propertyValue == null
                    ? null
                    : super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        });
    }

    /**
     * load yaml as object
     *
     * @param yaml yaml
     * @param type type
     * @param <T> type
     * @return object
     */
    public static <T> T loadAs(String yaml, Class<T> type) {
        return get().loadAs(yaml, type);
    }

    /**
     * dump object as yaml
     *
     * @param obj obj
     * @return yaml
     */
    public static String dump(Object obj) {
        return get().dumpAsMap(obj);
    }
}