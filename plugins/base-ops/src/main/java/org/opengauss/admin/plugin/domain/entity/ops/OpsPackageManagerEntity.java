/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsPackageManagerEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsPackageManagerEntity.java
 *
 * -------------------------------------------------------------------------
 */

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
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
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
    private String name;
    @TableField(exist = false)
    private String urlPrefix;
    private String os;
    private String osVersion;
    private String cpuArch;
    private String packageVersion;
    private String packageVersionNum;
    private String packageUrl;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private UploadInfo packagePath;
    private String type;
    private String source;
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

    public OpsPackageVO toVO() {
        OpsPackageVO vo = new OpsPackageVO();
        vo.setName(name);
        vo.setPackageId(packageId);
        vo.setPackageVersion(packageVersion);
        vo.setPackageVersionNum(packageVersionNum);
        vo.setOs(os);
        vo.setOsVersion(osVersion);
        vo.setCpuArch(cpuArch);
        vo.setRemark(getRemark());
        vo.setType(type);
        vo.setSource(source);
        vo.setPackageUrl(packageUrl);
        vo.setPackagePath(packagePath);
        vo.setUpdateTime(getUpdateTime());
        return vo;
    }
}
