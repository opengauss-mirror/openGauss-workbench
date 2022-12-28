package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/8/15 22:20
 **/
@Data
public class DownloadBody {
    @NotBlank(message = "The resource path cannot be empty")
    private String resourceUrl;
    @NotBlank(message = "The target path cannot be empty")
    private String targetPath;
    @NotBlank(message = "The file name cannot be empty")
    private String fileName;
    @NotBlank(message = "The service ID cannot be empty")
    private String businessId;
}
