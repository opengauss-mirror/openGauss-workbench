package org.opengauss.admin.common.enums;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Method
 *
 * @author xielibo
 */
public enum HttpMethod {
    //GET
    GET,
    //HEAD
    HEAD,
    //POST
    POST,
    //PUT
    PUT,
    //PATCH
    PATCH,
    //DELETE
    DELETE,
    //OPTIONS
    OPTIONS,
    //TRACE
    TRACE;

    private static final Map<String, HttpMethod> MAPPINGS = new HashMap<>(16);

    static {
        for (HttpMethod httpMethod : values()) {
            MAPPINGS.put(httpMethod.name(), httpMethod);
        }
    }

    @Nullable
    public static HttpMethod resolve(@Nullable String method) {
        return (method != null ? MAPPINGS.get(method) : null);
    }

    public boolean matches(String method) {
        return (this == resolve(method));
    }
}
