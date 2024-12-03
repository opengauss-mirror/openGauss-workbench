/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 *  JsonUtils.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/util/JsonUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * 基于 jackson 的json装换工具
 *
 * @since 2024-6-10
 */
public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    static {
        MAPPER.registerModule(new JavaTimeModule()); // 解析java.time.OffsetDateTime

        // 关闭时间戳
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * 时间格式化为不带时区格式的
     *
     * @param object object
     * @return string
     */
    public static String objToStrWithoutTZ(Object object) {
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 避免metadata.creationTimestamp

        // 指定设置后的格式,格式转换失败，默认是个长整型时间戳，前端需要“yyyy-MM-dd HH:mm:ss”字符串
        MAPPER.setDateFormat(sdf);
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("object -> json error ", e);
            return "";
        }
    }

    /**
     * 时间格式化为带时区格式的
     *
     * @param object object
     * @return string
     */
    public static String objToStrWithTZ(Object object) {
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 避免metadata.creationTimestamp

        // 指定设置后的格式,格式转换失败，默认是个长整型时间戳，k8s端是“yyyy-MM-dd'T'HH:mm:ss'Z'”字符串
        MAPPER.setDateFormat(sdf);
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("object -> json error ", e);
            return "";
        }
    }

    /**
     * 将object反序列化成List集合
     *
     * @param object       object
     * @param tClass       class
     * @param isContainsTZ 是否带时区格式
     * @return list
     */
    public static <T> List<T> objToArray(Object object, Class<T> tClass, boolean isContainsTZ) {
        String value;
        if (isContainsTZ) {
            value = objToStrWithTZ(object);
        } else {
            value = objToStrWithoutTZ(object);
        }
        try {
            if (value == null || value.isEmpty()) {
                return Collections.emptyList();
            }
            return MAPPER.readValue(value, getCollectionType(tClass));
        } catch (IOException e) {
            String className = tClass.getSimpleName();
            logger.error("value -> List<object> error , object is {} , value is {} , error is {}", className, value, e);
        }
        return Collections.emptyList();
    }

    private static JavaType getCollectionType(Class<?>... elementClasses) {
        return MAPPER.getTypeFactory().constructParametricType(List.class, elementClasses);
    }
}
