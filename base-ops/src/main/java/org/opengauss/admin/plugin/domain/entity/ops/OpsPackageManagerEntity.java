package org.opengauss.admin.plugin.domain.entity.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

import java.text.MessageFormat;

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
    private OpenGaussVersionEnum packageVersion;
    private String packageVersionNum;
    private String packageUrl;

    public OpsPackageManagerEntity populatePackageUrl(String installPackageUrlPrefix) {
        String resTemplate = "{0}/{1}/{2}/{3}";

        while (StrUtil.isNotEmpty(urlPrefix) && urlPrefix.endsWith("/")){
            urlPrefix = urlPrefix.substring(0,urlPrefix.length()-1);
        }

        if (StrUtil.isEmpty(urlPrefix)){
            urlPrefix = installPackageUrlPrefix;
        }

        OpenGaussSupportOSEnum osInfoEnum = OpenGaussSupportOSEnum.of(os, null, cpuArch);

        String cpuArch;
        switch (osInfoEnum){
            case CENTOS_X86_64: cpuArch = "x86";break;
            case OPENEULER_AARCH64: cpuArch = "arm";break;
            case OPENEULER_X86_64: cpuArch = "x86_openEuler";break;
            default: throw new OpsException("cpu architecture information error");
        }

        StringBuilder packageName = new StringBuilder("openGauss-");

        if (OpenGaussVersionEnum.LITE == packageVersion){
            packageName.append("Lite-");
        }

        packageName.append(packageVersionNum).append("-");

        switch (osInfoEnum){
            case CENTOS_X86_64: packageName.append("CentOS");break;
            default: packageName.append("openEuler");
        }

        switch (packageVersion){
            case ENTERPRISE: packageName.append("-64bit-all.tar.gz");break;
            case LITE:
                switch (osInfoEnum){
                    case OPENEULER_AARCH64: packageName.append("-aarch64.tar.gz");break;
                    default: packageName.append("-x86_64.tar.gz");break;
                }
                break;
            case MINIMAL_LIST:packageName.append("-64bit.tar.bz2");break;
        }

        String formatRes = MessageFormat.format(resTemplate, urlPrefix, packageVersionNum, cpuArch, packageName);
        this.setPackageUrl(formatRes);

        return this;
    }
}
