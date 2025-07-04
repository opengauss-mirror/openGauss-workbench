/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.vo;

import io.opentelemetry.api.common.Attributes;
import lombok.Data;

import java.util.concurrent.atomic.AtomicReference;

/**
 * MetricData
 *
 * @author: wangchao
 * @Date: 2025/5/6 09:55
 * @Description: MetricData
 * @since 7.0.0-RC2
 **/
@Data
public class MetricData {
    private AtomicReference<Double> value = new AtomicReference<>(0.0);
    private Attributes attributes = Attributes.empty();
}

