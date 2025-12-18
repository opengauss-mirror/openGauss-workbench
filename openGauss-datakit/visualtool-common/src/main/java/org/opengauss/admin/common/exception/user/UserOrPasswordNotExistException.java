/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.exception.user;

/**
 * The user password is incorrect or does not meet the specification exception class
 *
 * @author: wangchao
 * @Date: 2025/11/10 10:24
 * @Description: UserOrPasswordNotExistException
 * @since 7.0.0-RC3
 **/
public class UserOrPasswordNotExistException extends UserException {
    private static final long serialVersionUID = 1L;

    /**
     * UserOrPasswordNotExistException
     */
    public UserOrPasswordNotExistException() {
        super("user.password.not.match", null);
    }
}
