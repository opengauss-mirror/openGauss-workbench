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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description: Base page DTO，when trans to mybatis by PageBaseQuery,select page will
 * return this DTO
 * @author: YangZiHao
 * @date: 2022/11/30 10:36
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BasePageDTO<T> implements MyPage<T> {
    private static final long serialVersionUID = 8545996863226528798L;

    private List<T> records = Collections.emptyList();
    private long total = 0;

    @JsonIgnore
    public long getSize() {
        return size;
    }
    private long size = 10;

    @JsonIgnore
    public long getCurrent() {
        return current;
    }
    public long getPageNum() {
        return current;
    }
    private long current = 1;

    @JsonIgnore
    public List<OrderItem> getOrders() {
        return orders;
    }
    private List<OrderItem> orders = new ArrayList<>();

    @JsonIgnore
    public boolean isOptimizeCountSql() {
        return optimizeCountSql;
    }
    private boolean optimizeCountSql = true;

    public List<T> getRecords() {
        return records;
    }
    private boolean isSearchCount = true;
    public BasePageDTO(long current, long size) {
        this(current, size, 0);
    }

    public BasePageDTO(long current, long size, long total) {
        this(current, size, total, true);
    }

    public BasePageDTO(long current, long size, boolean isSearchCount) {
        this(current, size, 0, isSearchCount);
    }

    public BasePageDTO(long current, long size, long total, boolean isSearchCount) {
        if (current > 1)
            this.current = current;
        this.size = size;
        this.total = total;
        this.isSearchCount = isSearchCount;
    }
    public boolean hasPrevious() {
        return this.current > 1;
    }
    public boolean hasNext() {
        return this.current < this.getPages();
    }
    @Override
    @Deprecated
    public String[] ascs() {
        return CollectionUtils.isNotEmpty(orders) ? mapOrderToArray(OrderItem::isAsc) : null;
    }
    private String[] mapOrderToArray(Predicate<OrderItem> filter) {
        List<String> columns = new ArrayList<>(orders.size());
        orders.forEach(i -> {
            if (filter.test(i)) {
                columns.add(i.getColumn());
            }
        });
        return columns.toArray(new String[0]);
    }
    private void removeOrder(Predicate<OrderItem> filter) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (filter.test(orders.get(i))) {
                orders.remove(i);
            }
        }
    }
    public BasePageDTO<T> addOrder(OrderItem... items) {
        orders.addAll(Arrays.asList(items));
        return this;
    }
    public BasePageDTO<T> addOrder(List<OrderItem> items) {
        orders.addAll(items);
        return this;
    }
    @Deprecated
    public BasePageDTO<T> setAscs(List<String> ascs) {
        return CollectionUtils.isNotEmpty(ascs) ? setAsc(ascs.toArray(new String[0])) : this;
    }
    @Deprecated
    public BasePageDTO<T> setAsc(String... ascs) {
        // Ensure the semantics of the original method set
        removeOrder(OrderItem::isAsc);
        for (String s : ascs) {
            addOrder(OrderItem.asc(s));
        }
        return this;
    }
    @Override
    @Deprecated
    public String[] descs() {
        return mapOrderToArray(i -> !i.isAsc());
    }
    @Deprecated
    public BasePageDTO<T> setDescs(List<String> descs) {
        // Ensure the semantics of the original method set
        if (CollectionUtils.isNotEmpty(descs)) {
            removeOrder(item -> !item.isAsc());
            for (String s : descs) {
                addOrder(OrderItem.desc(s));
            }
        }
        return this;
    }
    @Deprecated
    public BasePageDTO<T> setDesc(String... descs) {
        setDescs(Arrays.asList(descs));
        return this;
    }

    @Override
    public List<OrderItem> orders() {
        return getOrders();
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    @JsonIgnore
    @Override
    public boolean optimizeCountSql() {
        return optimizeCountSql;
    }

    @Override
    @JsonIgnore
    public boolean isSearchCount() {
        if (total < 0) {
            return false;
        }
        return isSearchCount;
    }

    @Override
    public long getPageSize() {
        return this.size;
    }

    @Override
    public MyPage<T> setPageSize(long size) {
        return this;
    }
}