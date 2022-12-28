package org.opengauss.admin.common.core.domain.model;

import lombok.Data;

/**
 * User Login DTO
 *
 * @author xielibo
 */
@Data
public class LoginBody {
    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * code
     */
    private String code;

}
