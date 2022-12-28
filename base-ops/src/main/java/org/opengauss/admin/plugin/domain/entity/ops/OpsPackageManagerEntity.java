package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.domain.BaseEntity;
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
    private String os;
    private String cpuArch;
    private OpenGaussVersionEnum packageVersion;
    private String packageVersionNum;
    private String packageUrl;
}
