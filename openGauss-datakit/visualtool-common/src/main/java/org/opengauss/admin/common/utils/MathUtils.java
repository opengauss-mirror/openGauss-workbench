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

package org.opengauss.admin.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MathUtils
 *
 * @author: wangchao
 * @Date: 2025/6/6 16:25
 * @since 7.0.0-RC2
 **/
public class MathUtils {
    private static final int SCALE_2 = 2;
    private static final int SCALE_4 = 4;
    private static final BigDecimal PERCENT = new BigDecimal(100);

    /**
     * add two value and format to 2 decimal places
     *
     * @param value1 value1
     * @param value2 value2
     * @return String
     */
    public static String add(String value1, String value2) {
        return new BigDecimal(value1).add(new BigDecimal(value2)).setScale(SCALE_2, RoundingMode.HALF_UP).toString();
    }

    /**
     * divide two value and format to 4 decimal places
     *
     * @param s s
     * @param s1 s1
     * @return String
     */
    public static String divide(String s, String s1) {
        return divide(s, s1, SCALE_4).toString();
    }

    /**
     * format to 2 decimal places
     *
     * @param s s
     * @return String
     */
    public static String formatScaleTwo(String s) {
        return new BigDecimal(s).setScale(SCALE_2, RoundingMode.HALF_UP).toString();
    }

    /**
     * <pre>
     * calculate the percentage of the dividend and divisor, and format to 2 decimal places
     * e.g. 1/10 = 10.00%
     * e.g. 1/3 = 33.33%
     * </pre>
     *
     * @param dividend dividend
     * @param divisorVal divisorVal
     * @return String
     */
    public static String percent(String dividend, String divisorVal) {
        return divide(dividend, divisorVal, SCALE_4).multiply(PERCENT)
            .setScale(SCALE_2, RoundingMode.HALF_UP)
            .toString();
    }

    private static BigDecimal divide(String s, String s1, int scale) {
        BigDecimal divisor = new BigDecimal(s1);
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(s).divide(divisor, scale, RoundingMode.HALF_UP);
    }
}
