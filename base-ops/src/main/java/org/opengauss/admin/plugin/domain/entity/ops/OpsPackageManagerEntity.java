package org.opengauss.admin.plugin.domain.entity.ops;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lhf
 * @date 2022/12/11 16:11
 **/
@Data
@TableName(value = "ops_package_manager", autoResultMap = true)
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
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private UploadInfo packagePath;
    private String type;
    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile file;

    public String getRealPath() {
        if (ObjectUtil.isNull(packagePath)) {
            return "";
        }
        return packagePath.getRealPath();
    }

    public String getFileName() {
        if (ObjectUtil.isNull(packagePath)) {
            return "";
        }
        return packagePath.getName();
    }
}
