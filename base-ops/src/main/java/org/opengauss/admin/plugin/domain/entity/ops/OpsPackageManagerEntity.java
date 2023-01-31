package org.opengauss.admin.plugin.domain.entity.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

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
        StringBuilder res = new StringBuilder();

        while (StrUtil.isNotEmpty(urlPrefix) && urlPrefix.endsWith("/")){
            urlPrefix = urlPrefix.substring(0,urlPrefix.length()-1);
        }

        if (StrUtil.isEmpty(urlPrefix)){
            urlPrefix = installPackageUrlPrefix;
        }

        res.append(urlPrefix);

        OpenGaussSupportOSEnum osInfoEnum = OpenGaussSupportOSEnum.of(os, null, cpuArch);

        res.append("/").append(packageVersionNum);

        switch (osInfoEnum){
            case CENTOS_X86_64:res.append("/").append("x86");break;
            case OPENEULER_ARCH64:res.append("/").append("arm");break;
            case OPENEULER_X86_64: res.append("/").append("x86_openEuler");break;
        }

        res.append("/").append("openGauss-");

        if (OpenGaussVersionEnum.LITE == packageVersion){
            res.append("Lite-");
        }

        res.append(packageVersionNum).append("-");

        switch (osInfoEnum){
            case CENTOS_X86_64: res.append("CentOS");break;
            default: res.append("openEuler");
        }

        switch (packageVersion){
            case ENTERPRISE: res.append("-64bit-all.tar.gz");break;
            case LITE:
                switch (osInfoEnum){
                    case OPENEULER_ARCH64: res.append("-aarch64.tar.gz");break;
                    default: res.append("-x86_64.tar.gz");break;
                }
                break;
            case MINIMAL_LIST:res.append("-64bit.tar.bz2");break;
        }

        this.setPackageUrl(res.toString());

        return this;
    }
}
