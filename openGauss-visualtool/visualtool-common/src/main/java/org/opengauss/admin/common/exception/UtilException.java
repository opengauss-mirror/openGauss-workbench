package org.opengauss.admin.common.exception;

/**
 * Tool Exception
 *
 * @author xielibo
 */
public class UtilException extends RuntimeException {

    public UtilException(Throwable e) {
        super(e.getMessage(), e);
    }

    public UtilException(String message) {
        super(message);
    }

    public UtilException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
