package org.opengauss.admin.plugin.domain.model.ops;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wangyl
 * @date 203/03/08 13:40
 **/
@Data
public class OpsPackageVO {
    private String packageId;
    private String os;
    private String cpuArch;
    private String packageVersion;
    private String packageVersionNum;
    private String packageUrl;
    private UploadInfo packagePath;
    private String type;
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile file;
    private String remark;

    public OpsPackageManagerEntity toEntity() {
        OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
        entity.setPackageId(packageId);
        entity.setOs(os);
        entity.setCpuArch(cpuArch);
        entity.setPackageVersion(packageVersion);
        entity.setPackageVersionNum(packageVersionNum);
        entity.setPackageUrl(packageUrl);
        entity.setPackagePath(packagePath);
        entity.setType(type);
        entity.setFile(file);
        entity.setRemark(remark);
        return entity;
    }
}
