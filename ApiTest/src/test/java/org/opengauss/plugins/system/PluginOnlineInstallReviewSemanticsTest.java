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
