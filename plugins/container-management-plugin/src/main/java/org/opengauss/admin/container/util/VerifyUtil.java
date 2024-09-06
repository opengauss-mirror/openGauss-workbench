/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 *  StorageUnitConverter.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/util/VerifyUtil.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 合法性判断，系统规格等参数校验
 *
 * @since 2024-6-10
 */
public class VerifyUtil {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[1-9]+[0-9]*$");
    private static final Pattern CPU_PATTERN = Pattern.compile("(^[1-9]+[0-9]*[cCmM]?$)|([1-9]\\d*\\.[0-9]{1,3}[cC]?)"
            + "|(0\\.[1-9]{1,3}[cC]?)");
    private static final Pattern MEMORY_PATTERN = Pattern.compile("(^[1-9]+[0-9]*[MG]i$)|([1-9]\\d*\\.?[0-9][MG]i)|"
            + "(0\\.[1-9][MG]i)");
    private static final Pattern NUMEBER_PATTERN = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");
    private static final String VALID_HOSTNAME_REGEX = "^(([a-z]|[a-z][a-z0-9\\-]*[a-z0-9])\\.)*"
            + "([a-z]|[a-z][a-z0-9\\-]*[a-z0-9])$";

    /**
     * 判断是否是正整数
     *
     * @param num 数字字符串
     * @return 是否为数字
     */
    public static boolean isInteger(String num) {
        Matcher m = INTEGER_PATTERN.matcher(num);
        return m.find();
    }

    /**
     * 判断cpu写法是否符合规范
     *
     * @param cpu cpu数
     * @return 是否符合规范
     */
    public static boolean validCpu(String cpu) {
        Matcher isNum = CPU_PATTERN.matcher(cpu);
        return isNum.matches();
    }

    /**
     * cpu规格转换（统一转换为c为单位（c省略）），不符合规范的返回0
     *
     * @param cpu cpu数
     * @return 转换结果
     */
    public static String convCpu(String cpu) {
        if (validCpu(cpu)) {
            if (cpu.toLowerCase(Locale.ROOT).endsWith("c")) {
                return cpu.substring(0, cpu.length() - 1);
            } else if (cpu.toLowerCase(Locale.ROOT).endsWith("m")) {
                String val = cpu.substring(0, cpu.length() - 1);
                BigDecimal bigDecimal = new BigDecimal(val);
                BigDecimal divide = bigDecimal.divide(BigDecimal.valueOf(1000));
                divide.setScale(3, RoundingMode.HALF_UP);
                return String.valueOf(divide.doubleValue());
            } else {
                return cpu;
            }
        } else {
            return "0"; // cpu书写不正确
        }
    }

    /**
     * cpu爆发倍数计算，返回数据的单位是c（已省略），不符合规范的返回0
     *
     * @param cpu   cpu数
     * @param times 倍数
     * @return cpu倍数计算结果
     */
    public static String multiCpu(String cpu, int times) {
        String cpuWithUnitC = convCpu(cpu);
        BigDecimal bigDecimal = new BigDecimal(cpuWithUnitC);
        BigDecimal multiply = bigDecimal.multiply(BigDecimal.valueOf(times));
        multiply.setScale(3, RoundingMode.HALF_UP);
        return String.valueOf(multiply.doubleValue());
    }

    /**
     * 判断内存写法是否符合规范
     *
     * @param memory 内存
     * @return 是否符合
     */
    public static boolean validMemory(String memory) {
        Matcher isNum = MEMORY_PATTERN.matcher(memory);
        return isNum.matches();
    }

    /**
     * 判断数值类型
     *
     * @param num 字符串
     * @return 是否为数字
     */
    public static boolean isNumeric(String num) {
        Matcher isNumberic = NUMEBER_PATTERN.matcher(num);
        return isNumberic.matches();
    }
}
