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
 * OpsPackageVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsPackageVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
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
