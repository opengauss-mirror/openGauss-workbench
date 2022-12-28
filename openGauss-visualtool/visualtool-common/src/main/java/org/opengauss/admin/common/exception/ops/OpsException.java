package org.opengauss.admin.common.exception.ops;

/**
 *
 * @author lhf
 * @date 2022/8/4 22:24
 **/
public class OpsException extends RuntimeException {
    private String message;

    public OpsException(String message) {
        super(message);
        this.message = message;
    }
}
