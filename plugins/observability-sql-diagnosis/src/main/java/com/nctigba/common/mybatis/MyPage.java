/**
 * Copyright (c) 2011-2022, baomidou (jobob@qq.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nctigba.common.mybatis;

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