package org.opengauss.admin.common.exception.file;

/**
 * File Size Limit Exception
 *
 * @author xielibo
 */
public class FileSizeLimitExceededException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileSizeLimitExceededException(long defaultMaxSize) {
        super("upload.exceed.maxSize", new Object[]{defaultMaxSize});
    }
}
