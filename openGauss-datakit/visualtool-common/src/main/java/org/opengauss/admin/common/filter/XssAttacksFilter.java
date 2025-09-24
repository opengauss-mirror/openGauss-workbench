/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * XssAttacksFilter.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/filter/XssAttacksFilter.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.filter;

import org.opengauss.admin.common.utils.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters to prevent XSS attacks
 *
 * @author xielibo
 */
public class XssAttacksFilter implements Filter {

    public List<String> excludes = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String excludesArrStr = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(excludesArrStr)) {
            String[] urlArr = excludesArrStr.split(",");
            for (int i = 0; urlArr != null && i < urlArr.length; i++) {
                excludes.add(urlArr[i]);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (doExcludeUrlHandler(req)) {
            chain.doFilter(request, response);
            return;
        }
        XssAttacksHttpServletRequestWrapper xssWrapper = new XssAttacksHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssWrapper, response);
    }

    private boolean doExcludeUrlHandler(HttpServletRequest request) {
        String url = request.getServletPath();
        String method = request.getMethod();
        if (method == null || method.matches("GET") || method.matches("DELETE")) {
            return true;
        }
        return StringUtils.matches(url, excludes);
    }
}
