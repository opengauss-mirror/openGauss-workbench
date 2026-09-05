/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
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
