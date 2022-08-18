package org.opengauss.admin.common.exception;

/**
 * Business Exception
 *
 * @author xielibo
 */
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * code
     */
    private Integer code;

    /**
     * message
     */
    private String message;

    /**
     * detailMessage
     */
    private String detailMessage;

    /**
     * parameterless constructor
     */
    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}