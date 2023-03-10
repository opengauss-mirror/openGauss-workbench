package org.opengauss.admin.plugin.domain.entity.ops;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lhf
 * @date 2022/12/11 16:11
 **/
@Data
@TableName("ops_package_manager")
@EqualsAndHashCode(callSuper = true)
public class OpsPackageManagerEntity extends BaseEntity {
    @TableId
    private String packageId;
    @TableField(exist = false)
    private String urlPrefix;
    private String os;
    private String cpuArch;
    private String packageVersion;
    private String packageVersionNum;
    private String packageUrl;
    private String packagePath;
    @TableField(exist = false)
    private UploadInfo uploadInfo;
    private String type;
    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile file;

    public String getFileRealPath() {
        if (ObjectUtil.isNull(uploadInfo)) {
            if (StrUtil.isNotEmpty(packagePath)) {
                uploadInfo = JSON.parseObject(packagePath, UploadInfo.class);
                return uploadInfo.getRealPath();
            } else {
                return null;
            }
        }
        return uploadInfo.getRealPath();
    }
}
