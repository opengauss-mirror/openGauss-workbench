/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.result;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * WebDsResultTest
 *
 * @since 2023-07-14
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class WebDsResultTest {
    @InjectMocks
    private WebDsResult webDsResult;

    @Test
    public void testOk() {
        WebDsResult.ok("text", "success");
    }

    @Test
    public void testError() {
        WebDsResult.error("text", "fail");
    }
}
