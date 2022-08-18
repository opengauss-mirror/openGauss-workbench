package org.opengauss.admin.common.exception.file;

/**
 * File Name Length Limit Exception
 *
 * @author xielibo
 */
public class FileNameLengthLimitExceededException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength) {
        super("upload.filename.exceed.length", new Object[]{defaultFileNameLength});
    }
}
