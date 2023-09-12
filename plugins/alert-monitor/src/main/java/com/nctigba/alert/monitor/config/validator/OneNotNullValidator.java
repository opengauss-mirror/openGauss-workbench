/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.config.annotation.OneNotNull;
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
