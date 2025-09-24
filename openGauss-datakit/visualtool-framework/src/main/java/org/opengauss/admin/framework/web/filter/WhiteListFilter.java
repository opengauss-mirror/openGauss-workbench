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
 * WhiteListFilter.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/web/filter/WhiteListFilter.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.web.filter;

import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.admin.system.service.ISysWhiteListService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @className: WhiteListFilter
 * @description: WhiteListFilter
 * @author: xielibo
 * @date: 2022-11-25 13:00
 **/
@Slf4j
@Component
@ConditionalOnProperty(value = "system.whitelist.enabled", havingValue = "true")
public class WhiteListFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        String ip = IpUtils.getIpAddr((HttpServletRequest) servletRequest);
        boolean isExists = SpringUtils.getBean(ISysWhiteListService.class).checkSingleIpExistsInWhiteList(ip);
        if (isExists) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                returnContent(servletResponse,
                    "Sorry,The current IP is not in the white list, please contact the administrator to add it before"
                        + " accessing.");
            } catch (Exception e) {
                log.error("error, message: {}", e.getMessage());
            }
        }
    }

    private void returnContent(ServletResponse response, String content) throws Exception{
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(content);

        } catch (IOException e) {
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
