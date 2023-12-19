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
 *  WebDsResultTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/result/WebDsResultTest.java
 *
 *  -------------------------------------------------------------------------
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
