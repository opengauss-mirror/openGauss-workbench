package org.opengauss.admin.framework.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.common.exception.user.UserPasswordNotMatchException;
import org.opengauss.admin.common.utils.RsaUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Login Service
 *
 * @author xielibo
 */
@Component
public class SysLoginService {

    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;


    /**
     * Login
     *
     * @param username username
     * @param password password
     * @param code     code
     * @return result
     */
    public String login(String username, String password, String code) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, RsaUtils.decryptByPrivateKey(password)));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new UserPasswordNotMatchException();
            } else {
                throw new ServiceException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }

}
