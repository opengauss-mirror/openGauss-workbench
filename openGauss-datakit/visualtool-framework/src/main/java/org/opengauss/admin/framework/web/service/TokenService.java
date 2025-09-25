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
 * TokenService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/web/service/TokenService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.web.service;

import com.github.benmanes.caffeine.cache.Cache;

import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.utils.RandomCharGenerator;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.ip.AddressUtils;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.common.utils.uuid.IdUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * TokenService
 *
 * @author: wangchao
 * @Date: 2025/9/25 10:07
 * @since 7.0.0-RC2
 **/
@Component
public class TokenService {
    private static final String HEADER = Constants.TOKEN_HEADER;
    private static final String SECRET;

    static {
        SECRET = RandomCharGenerator.generateRandomBased128String();
    }

    @Autowired
    private Cache<String, Object> loginCache;

    /**
     * Get logged in user
     *
     * @return loginUser
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUser user = (LoginUser) loginCache.asMap().get(userKey);
                setLoginUser(user);
                return user;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Set login user
     */
    public void setLoginUser(LoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * delete login user
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            loginCache.asMap().remove(userKey);
        }
    }

    /**
     * create token
     *
     * @param loginUser user
     * @return token
     */
    public String createToken(LoginUser loginUser) {
        String token = IdUtils.fastUuid();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        return createToken(claims);
    }

    /**
     * Refresh Token Validity Period
     */
    public void refreshToken(LoginUser loginUser) {
        String userKey = getTokenKey(loginUser.getToken());
        loginCache.put(userKey, loginUser);
    }

    /**
     * setUserAgent
     *
     * @param loginUser login user
     */
    public void setUserAgent(LoginUser loginUser) {
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIp(ip));
    }

    /**
     * create Token
     *
     * @param claims claims
     * @return token
     */
    private String createToken(Map<String, Object> claims) {
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }

    /**
     * parse token
     *
     * @param token token
     */
    private Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

    /**
     * get Username
     *
     * @param token token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * get token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return Constants.LOGIN_TOKEN_KEY + uuid;
    }
}
