package org.opengauss.admin.common.exception.user;


import org.opengauss.admin.common.exception.base.BaseException;

/**
 * User Exception
 *
 * @author xielibo
 */
public class UserException extends BaseException {
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args) {
        super("user", code, args, null);
    }
}
