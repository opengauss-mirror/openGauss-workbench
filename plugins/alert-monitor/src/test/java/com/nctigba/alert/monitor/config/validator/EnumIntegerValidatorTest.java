/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import com.nctigba.alert.monitor.config.annotation.EnumInteger;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyWay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintValidatorContext;

/**
 * EnumIntegerValidatorTest
 *
 * @since 2023/9/14 16:42
 */
@RunWith(SpringRunner.class)
public class EnumIntegerValidatorTest {
    @InjectMocks
    private EnumIntegerValidator validator;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    public void testIsValid() throws NoSuchFieldException {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType(CommonConstants.WE_COM).setName("name").setSendWay(0).setPersonId("1").setDeptId("1");
        EnumInteger annotation = notifyWay.getClass().getDeclaredField("sendWay").getAnnotation(EnumInteger.class);
        validator.initialize(annotation);
        validator.isValid(notifyWay.getSendWay(), context);
    }

    @Test
    public void testIsValidNull() throws NoSuchFieldException {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType(CommonConstants.WE_COM).setName("name").setSendWay(2).setPersonId("1").setDeptId("1");
        EnumInteger annotation = notifyWay.getClass().getDeclaredField("sendWay").getAnnotation(EnumInteger.class);
        validator.initialize(annotation);
        validator.isValid(notifyWay.getSendWay(), context);
    }
}
