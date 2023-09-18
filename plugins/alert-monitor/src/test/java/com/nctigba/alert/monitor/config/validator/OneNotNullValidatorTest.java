/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import com.nctigba.alert.monitor.config.annotation.OneNotNull;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyWay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintValidatorContext;

/**
 * OneNotNullValidatorTest
 *
 * @since 2023/9/14 16:01
 */
@RunWith(SpringRunner.class)
public class OneNotNullValidatorTest {
    @InjectMocks
    private OneNotNullValidator validator;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    public void testIsValid() {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType(CommonConstants.WE_COM).setName("name").setSendWay(0).setPersonId("1").setDeptId("1");
        OneNotNull annotation = notifyWay.getClass().getAnnotation(OneNotNull.class);
        validator.initialize(annotation);
        validator.isValid(notifyWay, context);
    }
}
