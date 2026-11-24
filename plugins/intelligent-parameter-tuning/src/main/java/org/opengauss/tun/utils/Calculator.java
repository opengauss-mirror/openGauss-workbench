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

package org.opengauss.tun.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Calculator
 *
 * @author liu
 * @since 2023-12-20
 */
public class Calculator {
    /**
     * calculatePercentage
     *
     * @param num1 num1
     * @param num2 num2
     * @return String
     */
    public static String calculatePercentage(String num1, String num2) {
        BigDecimal bigDecimalNum1 = new BigDecimal(num1);
        BigDecimal bigDecimalNum2 = new BigDecimal(num2);
        BigDecimal difference = bigDecimalNum1.subtract(bigDecimalNum2);
        BigDecimal percentage = difference.divide(bigDecimalNum1, 2, RoundingMode.HALF_UP);
        int roundedPercentage = percentage.multiply(new BigDecimal(100)).intValue();
        return String.format(Locale.ENGLISH, "%d%%", roundedPercentage);
    }
}
