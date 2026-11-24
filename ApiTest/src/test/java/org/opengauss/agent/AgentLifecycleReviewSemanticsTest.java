/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent;

import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * Pure review-regression checks for Agent lifecycle status polling.
 *
 * @since 2026/08/26
 */
public class AgentLifecycleReviewSemanticsTest {
    @Test
    public void stopFinalStoppedPasses() throws InterruptedException {
        AgentLifecycleApiTest.waitForExpectedStatus(() -> "stop", "stop", 1, Duration.ZERO);
    }

    @Test
    public void stopRunningThenStoppedPasses() throws InterruptedException {
        Iterator<String> statuses = List.of("running", "stop").iterator();
        AgentLifecycleApiTest.waitForExpectedStatus(statuses::next, "stop", 2, Duration.ZERO);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void stopRunningUntilTimeoutFails() throws InterruptedException {
        AgentLifecycleApiTest.waitForExpectedStatus(() -> "running", "stop", 2, Duration.ZERO);
    }
}
