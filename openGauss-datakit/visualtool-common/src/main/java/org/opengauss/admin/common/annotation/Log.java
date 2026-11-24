/*
 * Copyright (c) 2022 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */


package org.opengauss.admin.common.annotation;


import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * log annotation
 *
 * @author xielibo
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * title
     */
    public String title() default "";

    /**
     * businessType
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     * operator type
     */
    public OperatorType operatorType() default OperatorType.OTHER;

    /**
     * is save request data
     */
    public boolean isSaveRequestData() default true;

    /**
     * is save response data
     */
    public boolean isSaveResponseData() default true;
}
