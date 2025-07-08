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

package org.opengauss.agent.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * MathUtils
 *
 * @author: wangchao
 * @Date: 2025/6/6 16:25
 * @since 7.0.0-RC2
 **/
public class MathUtils {
    private static final long LONG_1024_1024_1024 = 1024 * 1024 * 1024;
    private static final BigDecimal DECIMAL_1024_1024_1024 = BigDecimal.valueOf(LONG_1024_1024_1024);
    private static final BigDecimal DECIMAL_1024 = BigDecimal.valueOf(1024);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final MathContext DIVISION_CONTEXT = new MathContext(6, RoundingMode.HALF_UP); // 预计算精度

    /**
     * calc double divide 1024*1024*1024
     *
     * @param dividend dividend
     * @return double
     */
    public static double convertBytesToGigabytes(long dividend) {
        return new BigDecimal(dividend).divide(DECIMAL_1024_1024_1024, DIVISION_CONTEXT).doubleValue();
    }

    /**
     * calc double divide 1024
     *
     * @param dividend dividend
     * @return double
     */
    public static double divide1024(long dividend) {
        return new BigDecimal(dividend).divide(DECIMAL_1024, DIVISION_CONTEXT).doubleValue();
    }

    /**
     * usage percentage percentage = (1.0 - available / total) * 100.0;
     *
     * @param available available
     * @param total total
     * @return double usage percentage
     */
    public static double calculateUtilizationPercentage(long available, long total) {
        if (total == 0 || available >= total) {
            return 0.0;
        }
        BigDecimal used = BigDecimal.valueOf(total - available);
        BigDecimal totalValue = BigDecimal.valueOf(total);
        return used.multiply(HUNDRED).divide(totalValue, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * safe parse double value to double, if parse failed, return Double.NaN
     *
     * @param value value
     * @return double
     */
    public static Double doubleValueOf(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
