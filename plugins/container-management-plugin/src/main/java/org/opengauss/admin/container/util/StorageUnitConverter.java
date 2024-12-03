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
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/util/StorageUnitConverter.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.util;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.container.constant.CommonConstant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储单位转换，磁盘存储数据类型转换
 *
 * @author freer
 * @since 2024-10-10
 */
public class StorageUnitConverter {
    private static final String DEFAULT_UNIT = "Gi";
    private static final String DEFAULT_CPU_UNIT = "C";

    /**
     * 适用于磁盘存储类型转换
     *
     * @param sValue value
     * @return GB数量
     */
    public static String convertToGB(String sValue) {
        if (StringUtils.isBlank(sValue)) {
            return sValue;
        }
        return convertToGB(sValue, DEFAULT_UNIT);
    }

    /**
     * 适用于磁盘存储类型转换
     *
     * @param sValue value
     * @param unit   转换后后缀单位可自定义
     * @return 带有单位的数据
     */
    public static String convertToGB(String sValue, String unit) {
        Unit u = Unit.distinguish(sValue);
        Double dValue = Double.valueOf(StringUtils.substring(sValue, 0, CharMatcher.javaLetter().indexIn(sValue)));
        if (u == Unit.GB) {
            return dValue + unit;
        }
        BigDecimal bdValue =
                BigDecimal.valueOf(dValue * Math.pow(u.type.rate, (u.ordinal() - Unit.GB.ordinal()))).setScale(2,
                        RoundingMode.HALF_UP);
        return bdValue + unit;
    }

    /**
     * 适用于内存转换，此方法不带单位。注意不要出现100mi这种，一般都是100Mi
     *
     * @param value value
     * @param to    要转换为的单位
     * @return string
     */
    public static String convertValue(String value, Unit to) {
        // 过滤内存为0
        if ("0".equals(value)) {
            return "0";
        }
        if (value.contains(CommonConstant.SMALLM) && !value.contains("mi")) {
            // m是特殊的，表示1/1000 byte，M正常
            String m = value.split(CommonConstant.SMALLM)[0];
            Double mDouble = Double.parseDouble(m);
            Double mKi = mDouble / (CommonConstant.NUM_THOUSAND * CommonConstant.NUM_SIZE_MEMORY);
            BigDecimal bdValue = BigDecimal.valueOf(mKi * Math.pow(TYPE.BINARY.rate,
                    (Unit.KB.ordinal() - to.ordinal()))).setScale(2, RoundingMode.HALF_UP);
            return bdValue.toString();
        }
        Unit u = Unit.distinguish(value);
        Double dValue = Double.valueOf(StringUtils.substring(value, 0, CharMatcher.javaLetter().indexIn(value)));
        BigDecimal bdValue =
                BigDecimal.valueOf(dValue * Math.pow(u.type.rate, (u.ordinal() - to.ordinal()))).setScale(2,
                        RoundingMode.HALF_UP);
        return bdValue.toString();
    }

    /**
     * 适用于CPU转换，此方法不带单位
     *
     * @param value cpu数值
     * @return cpu核数
     */
    public static String convertCPUValue(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.endsWith(DEFAULT_CPU_UNIT)) {
            return value;
        }
        if (value.contains(CommonConstant.SMALLM)) {
            String m = value.split(CommonConstant.SMALLM)[0];
            Double mDouble = Double.parseDouble(m);
            Double core = mDouble / (CommonConstant.NUM_THOUSAND);
            BigDecimal bdValue = BigDecimal.valueOf(core).setScale(2, RoundingMode.HALF_UP);
            return bdValue + DEFAULT_CPU_UNIT;
        }
        Double core = Double.valueOf(value);
        BigDecimal bdValue = BigDecimal.valueOf(core).setScale(0, RoundingMode.HALF_UP);
        return bdValue + DEFAULT_CPU_UNIT;
    }

    /**
     * 枚举类型TYPE
     *
     * @since 2024-5-20
     */
    public enum TYPE {
        DECIMAL(1000),
        BINARY(1024);
        private final int rate;

        TYPE(int rate) {
            this.rate = rate;
        }
    }

    /**
     * 枚举类型Unit
     *
     * @since 2024-5-20
     */
    public enum Unit {
        B("B"), KB("Ki"), MB("Mi"), GB("Gi"), TB("Ti"), PB("Pi"), EB("Ei"), ZB("Zi"), YB("Yi");
        private final String unit;
        private TYPE type;

        Unit(String unit) {
            this.unit = unit;
        }

        // 定义映射表
        private static final Map<String, Unit> UNIT_MAP = new HashMap<>();
        private static final Map<String, TYPE> TYPE_MAP = new HashMap<>();

        static {
            // 初始化映射表
            addMapping("b", B, TYPE.BINARY);
            addMapping("byte", B, TYPE.BINARY);
            addMapping("kb", KB, TYPE.BINARY);
            addMapping("ki", KB, TYPE.BINARY);
            addMapping("k", KB, TYPE.DECIMAL);
            addMapping("mb", MB, TYPE.BINARY);
            addMapping("mi", MB, TYPE.BINARY);
            addMapping("m", MB, TYPE.DECIMAL);
            addMapping("gb", GB, TYPE.BINARY);
            addMapping("gi", GB, TYPE.BINARY);
            addMapping("g", GB, TYPE.DECIMAL);
            addMapping("tb", TB, TYPE.BINARY);
            addMapping("ti", TB, TYPE.BINARY);
            addMapping("t", TB, TYPE.DECIMAL);
            addMapping("pb", PB, TYPE.BINARY);
            addMapping("pi", PB, TYPE.BINARY);
            addMapping("p", PB, TYPE.DECIMAL);
            addMapping("eb", EB, TYPE.BINARY);
            addMapping("ei", EB, TYPE.BINARY);
            addMapping("e", EB, TYPE.DECIMAL);
            addMapping("zb", ZB, TYPE.BINARY);
            addMapping("zi", ZB, TYPE.BINARY);
            addMapping("z", ZB, TYPE.DECIMAL);
            addMapping("yb", YB, TYPE.BINARY);
            addMapping("yi", YB, TYPE.BINARY);
            addMapping("y", YB, TYPE.DECIMAL);
        }

        private static void addMapping(String key, Unit unit, TYPE type) {
            UNIT_MAP.put(key, unit);
            TYPE_MAP.put(key, type);
        }

        /**
         * distinguish
         *
         * @param strValue strValue
         * @return Unit
         */
        public static Unit distinguish(String strValue) {
            int index = CharMatcher.javaLetter().indexIn(strValue);
            String unitLocal = StringUtils.substring(strValue, index).toLowerCase();
            // 从映射表中查找
            Unit target = UNIT_MAP.getOrDefault(unitLocal, GB); // 默认返回 GB
            target.type = TYPE_MAP.getOrDefault(unitLocal, TYPE.DECIMAL); // 默认类型为 DECIMAL
            return target;
        }
    }
}
