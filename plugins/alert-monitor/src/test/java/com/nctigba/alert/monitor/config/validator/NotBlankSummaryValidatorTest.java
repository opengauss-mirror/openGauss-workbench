/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyWay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintValidatorContext;

/**
 * NotBlankSummaryValidatorTest
 *
 * @since 2023/9/14 14:30
 */
@RunWith(SpringRunner.class)
public class NotBlankSummaryValidatorTest {
    @InjectMocks
    private NotBlankSummaryValidator notBlankSummaryValidator;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    public void testIsValid() {
        NotifyWay notifyWay = new NotifyWay();
        notifyWay.setNotifyType(CommonConstants.EMAIL).setName("name").setEmail("123@test.com").setSendWay(1);
        notBlankSummaryValidator.isValid(notifyWay, context);
    }
}
