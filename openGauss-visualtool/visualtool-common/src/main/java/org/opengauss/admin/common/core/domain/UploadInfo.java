package org.opengauss.admin.common.core.domain;

import lombok.Data;
import org.opengauss.admin.common.core.vo.UploadInfoVO;

import javax.validation.constraints.NotEmpty;

/**
 * File upload common info
 *
 * @author wangyl
 */
@Data
public class UploadInfo {
    /**
     * file upload path
     */
    @NotEmpty(message = "file path cannot be empty")
    private String realPath;
    /**
     * real file name
     */
    private String realName;
    /**
     * file name for UI
     */
    private String name;

    @Override
    public String toString() {
        return String.format("Real path: %s, File name: %s", realPath, name);
    }

    public UploadInfoVO toVO() {
        UploadInfoVO vo = new UploadInfoVO();
        vo.setName(name);
        vo.setRealName(realName);
        vo.setRealPath(realPath);
        return vo;
    }
}
