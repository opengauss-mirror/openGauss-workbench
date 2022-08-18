package org.opengauss.admin.framework.web.filter;

import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.admin.system.service.ISysWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String ip = IpUtils.getIpAddr((HttpServletRequest) servletRequest);
        boolean exists = SpringUtils.getBean(ISysWhiteListService.class).checkIpExistsInWhiteList(ip);
        if (exists) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                returnContent(servletResponse,"Sorry,The current IP is not in the white list, please contact the administrator to add it before accessing.");
            } catch (Exception e) {
                e.printStackTrace();
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
