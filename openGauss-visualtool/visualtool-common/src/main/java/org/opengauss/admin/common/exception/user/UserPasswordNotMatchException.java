package org.opengauss.admin.common.exception.user;

/**
 * The user password is incorrect or does not meet the specification exception class
 *
 * @author xielibo
 */
public class UserPasswordNotMatchException extends UserException {
    private static final long serialVersionUID = 1L;

    public UserPasswordNotMatchException() {
        super("user.password.not.match", null);
    }
}
