/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class YamlUtil {
    public static Yaml get() {
        return new Yaml(new Representer() {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                    Tag customTag) {
                return propertyValue == null ? null
                        : super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        });
    }

    public static <T> T loadAs(String yaml, Class<T> type) {
        return get().loadAs(yaml, type);
    }

    public static String dump(Object obj) {
        return get().dumpAsMap(obj);
    }
}