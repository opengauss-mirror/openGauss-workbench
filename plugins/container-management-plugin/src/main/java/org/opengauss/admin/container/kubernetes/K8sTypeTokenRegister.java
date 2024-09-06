/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * K8sTypeTokenRegister.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/K8sTypeTokenRegister.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于注册记录class--TypeToken对应关系
 * 保证CrdApis的扩展性
 *
 * @author wxy
 * @since 2024-7-12
 */
public class K8sTypeTokenRegister {
    private static final Map<Class, Type> typeMap = new HashMap<>(8);

    private static final Type DEFAULT_TYPE = new TypeToken<Object>() {
    }.getType();

    /**
     * 注册typeToken
     *
     * @param clazz class
     * @param type  com.google.gson.reflect.TypeToken设置好范型getType()
     */
    public static void register(Class clazz, Type type) {
        typeMap.put(clazz, type);
    }

    /**
     * 查询typeToken(若未注册默认返回Object Type)
     *
     * @param clazz class
     * @return Type
     */
    public static Type getType(Class clazz) {
        Type type = typeMap.get(clazz);
        return type == null ? DEFAULT_TYPE : type;
    }
}
