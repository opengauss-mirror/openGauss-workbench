/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  MyPage.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/MyPage.java
 *
 *  -------------------------------------------------------------------------
 */
package com.nctigba.observability.sql.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @description: Default page result
 * @author: YangZiHao
 * @date: 2022/11/22 22:20
 */
public interface MyPage<T> extends IPage<T> {

    @Deprecated
    default String[] descs() {
        return null;
    }

    @Deprecated
    default String[] ascs() {
        return null;
    }

    List<OrderItem> orders();

    default Map<Object, Object> condition() {
        return null;
    }

    default boolean optimizeCountSql() {
        return true;
    }

    default boolean isSearchCount() {
        return true;
    }

    default long offset() {
        return getCurrent() > 0 ? (getCurrent() - 1) * getSize() : 0;
    }

    default long getPages() {
        if (getSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    default MyPage<T> setPages(long pages) {
        // to do nothing
        return this;
    }

    List<T> getRecords();


    MyPage<T> setRecords(List<T> records);

    long getTotal();

    MyPage<T> setTotal(long total);

    long getPageSize();

    MyPage<T> setPageSize(long size);

    long getCurrent();

    MyPage<T> setCurrent(long current);

    @SuppressWarnings("unchecked")
    default <R> MyPage<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getRecords().stream().map(mapper).collect(toList());
        return ((MyPage<R>) this).setRecords(collect);
    }
}