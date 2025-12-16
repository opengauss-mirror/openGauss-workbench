/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

import java.util.List;

/**
 * Portal Type
 *
 * @since 2025/6/23
 */
@Getter
public enum PortalType {
    MYSQL_ONLY("Only support MySQL migration", List.of(DbTypeEnum.MYSQL)),
    MULTI_DB("Support PostgreSQL/Milvus/Elasticsearch migration",
            List.of(DbTypeEnum.POSTGRESQL, DbTypeEnum.MILVUS, DbTypeEnum.ELASTICSEARCH)),
    ;

    PortalType(String description, List<DbTypeEnum> supportedDbTypes) {
        this.description = description;
        this.supportedDbTypes = supportedDbTypes;
    }

    private final String description;
    private final List<DbTypeEnum> supportedDbTypes;
}
