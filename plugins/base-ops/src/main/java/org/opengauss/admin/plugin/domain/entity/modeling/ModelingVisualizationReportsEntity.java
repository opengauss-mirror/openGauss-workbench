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
 * ModelingVisualizationReportsEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/modeling/ModelingVisualizationReportsEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_visualization_reports
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_visualization_reports")
public class ModelingVisualizationReportsEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String url;

    /**
     *
     */
    private String intro;

    /**
     *
     */
    private Long dataFlowId;

    /**
     *
     */
    private String paramsJson;

    /**
     *
     */
    private Long type;

    /**
     *
     */
    private String resourceUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
