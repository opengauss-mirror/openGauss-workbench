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
 *  OneNotNullValidator.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/validator/OneNotNullValidator.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.validator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.model.validator.annotation.OneNotNull;
import com.nctigba.alert.monitor.constant.CommonConstants;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Check that at least one field is not empty
 *
 * @since 2023/9/5 11:25
 */
public class OneNotNullValidator implements ConstraintValidator<OneNotNull, Object> {
    private String conditionalField;
    private List<String> conditionalValues;
    private List<String> checkFields;

    @Override
    public void initialize(OneNotNull constraintAnnotation) {
        conditionalField = constraintAnnotation.conditionalField();
        conditionalValues = Arrays.asList(constraintAnnotation.conditionalValues());
        checkFields = Arrays.asList(constraintAnnotation.checkFields());
    }

    @Override
    public boolean isValid(Object entity, ConstraintValidatorContext context) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        if (StrUtil.isNotBlank(conditionalField)) {
            Object propertyValue = beanWrapper.getPropertyValue(conditionalField);
            if (propertyValue == null || !conditionalValues.contains(propertyValue.toString())) {
                return true;
            }
        }
        for (String checkField : checkFields) {
            Object propertyValue = beanWrapper.getPropertyValue(checkField);
            if (propertyValue instanceof String && StrUtil.isNotBlank((String) propertyValue)) {
                return true;
            }
            if (propertyValue instanceof List && CollectionUtil.isNotEmpty((List) propertyValue)) {
                return true;
            }
            if (propertyValue instanceof Map && CollectionUtil.isNotEmpty((Map<?, ?>) propertyValue)) {
                return true;
            }
            if ((propertyValue instanceof Integer || propertyValue instanceof Long) && propertyValue != null) {
                return true;
            }
        }

        context.disableDefaultConstraintViolation();
        String fields = String.join(CommonConstants.DELIMITER, checkFields);
        context.buildConstraintViolationWithTemplate(
            "(" + fields + ")" + context.getDefaultConstraintMessageTemplate()).addConstraintViolation();
        return false;
    }
}
