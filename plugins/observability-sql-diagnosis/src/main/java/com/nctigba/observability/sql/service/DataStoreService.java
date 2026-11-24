/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.model.vo.DataStoreVO;

import java.util.List;

/**
 * DataStoreService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface DataStoreService {
    /**
     * Store data
     *
     * @param list collection data
     */
    void storeData(List<DataStoreVO> list);

    /**
     * Get stored data
     *
     * @param item collection item
     * @return DataStoreVO
     */
    DataStoreVO getData(CollectionItem<?> item);

    /**
     * Get all collection item
     *
     * @return List
     */
    List<CollectionItem<?>> getCollectionItem();

    /**
     * Clear data
     *
     */
    void clearData();
}
