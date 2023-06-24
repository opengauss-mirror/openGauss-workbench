/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history;

import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DataStoreConfig
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataStoreConfig {
    private CollectionItem<?> collectionItem;
    private Object collectionData;
    private Integer count;
}
