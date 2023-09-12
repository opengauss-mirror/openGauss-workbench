/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.exception;

/**
 * ExporterException.java
 *
 * @since 2023-08-25
 */
public class ExporterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExporterException() {
        super();
    }

    public ExporterException(String message) {
        super(message);
    }

    public ExporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExporterException(Throwable cause) {
        super(cause);
    }
}