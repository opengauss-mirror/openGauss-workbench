/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.plugins.system;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

/**
 * Pure review-regression checks for the online-install fixture selection.
 *
 * @since 2026/08/26
 */
public class PluginOnlineInstallReviewSemanticsTest {
    @Test
    public void officialRegistryIncludesEveryFrozenRc3PluginUrl() {
        Assert.assertEquals(PluginOnlineInstallApiTest.officialRc3PluginUrls().keySet(), Set.of(
            "alert-monitor", "webds-plugin", "observability-log-search", "observability-instance",
            "data-migration", "base-ops", "observability-sql-diagnosis", "compatibility-assessment",
            "container-management"));
    }

    @Test
    public void fixtureSelectionSkipsCoreAndLoadedPlugins() {
        String pluginId = PluginOnlineInstallApiTest.selectSafeFixture(
            List.of("base-ops", "webds-plugin", "alert-monitor", "data-migration"),
            Set.of("alert-monitor"));

        Assert.assertEquals(pluginId, "data-migration");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void fixtureSelectionFailsClearlyWhenNoSafePluginIsAvailable() {
        PluginOnlineInstallApiTest.selectSafeFixture(List.of("base-ops", "webds-plugin"), Set.of());
    }

    @Test
    public void websocketSetupKeepsHostnameVerificationProperty() throws Exception {
        String property = "jdk.internal.httpclient.disableHostnameVerification";
        String before = System.getProperty(property);
        try {
            System.setProperty(property, "review-sentinel");
            PluginOnlineInstallApiTest.createProgressHttpClient();
            Assert.assertEquals(System.getProperty(property), "review-sentinel");
        } finally {
            if (before == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, before);
            }
        }
    }
}
