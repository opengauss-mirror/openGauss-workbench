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
 *  NotBlankSummaryValidator.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/validator/NotBlankSummaryValidator.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.validator;

import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankSummary;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Check if the field is not blank based on conditions
 *
 * @since 2023/9/5 10:23
 */
public class NotBlankSummaryValidator implements ConstraintValidator<NotBlankSummary, Object> {
    @Override
    public boolean isValid(Object entity, ConstraintValidatorContext context) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (checkField(field, beanWrapper, context)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean checkField(Field field, BeanWrapper beanWrapper, ConstraintValidatorContext context) {
        if (field.isAnnotationPresent(NotBlankConditional.class)) {
            NotBlankConditional annotation = field.getAnnotation(NotBlankConditional.class);
            if (isNotValidByConditional(annotation, beanWrapper, field)) {
                resetContext(field, context);
                return false;
            }
        }
        if (field.isAnnotationPresent(NotBlankConditional.List.class)) {
            NotBlankConditional.List annotationList = field.getAnnotation(NotBlankConditional.List.class);
            NotBlankConditional[] notBlankConditionals = annotationList.value();
            for (NotBlankConditional annotation : notBlankConditionals) {
                if (isNotValidByConditional(annotation, beanWrapper, field)) {
                    resetContext(field, context);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNotValidByConditional(NotBlankConditional annotation, BeanWrapper beanWrapper, Field field) {
        String conditionalField = annotation.conditionalField();
        List<String> conditionalValues = Arrays.asList(annotation.conditionalValues());
        Object propertyValue = beanWrapper.getPropertyValue(conditionalField);
        Object fieldValue = beanWrapper.getPropertyValue(field.getName());
        return propertyValue != null && conditionalValues.contains(propertyValue.toString())
            && fieldValue instanceof String && StrUtil.isBlank((String) fieldValue);
    }

    private void resetContext(Field field, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                field.getName() + context.getDefaultConstraintMessageTemplate())
            .addPropertyNode(field.getName()).addConstraintViolation();
    }
}
