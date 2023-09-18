/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import com.nctigba.alert.monitor.config.annotation.EnumString;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyWay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintValidatorContext;

/**
 * EnumStringValidatorTest
 *
 * @since 2023/9/14 17:00
 */
@RunWith(SpringRunner.class)
public class EnumStringValidatorTest {
    @InjectMocks
    private EnumStringValidator validator;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    public void testIsValid() throws NoSuchFieldException {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType(CommonConstants.WE_COM).setName("name").setSendWay(0).setPersonId("1").setDeptId("1");
        EnumString annotation = notifyWay.getClass().getDeclaredField("notifyType").getAnnotation(EnumString.class);
        validator.initialize(annotation);
        validator.isValid(notifyWay.getNotifyType(), context);
    }

    @Test
    public void testIsValidNull() throws NoSuchFieldException {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType("abc").setName("name");
        EnumString annotation = notifyWay.getClass().getDeclaredField("notifyType").getAnnotation(EnumString.class);
        validator.initialize(annotation);
        validator.isValid(notifyWay.getNotifyType(), context);
    }
}
