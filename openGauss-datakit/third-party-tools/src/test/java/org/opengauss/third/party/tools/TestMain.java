/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools;

import org.junit.Assert;
import org.opengauss.third.party.tools.csv.CsvUtils;
import org.opengauss.third.party.tools.entity.BaseInstallInfo;
import org.opengauss.third.party.tools.entity.PortalInstallInfo;
import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test main class
 *
 * @since 2026/6/8
 */
public class TestMain {
    public static void main(String[] args) throws IOException {
        assertRegistry();
        assertSave();
        assertDelete();
    }

    private static void assertRegistry() throws IOException {
        PortalInstallInfo portalInstallInfo1 = new PortalInstallInfo();
        portalInstallInfo1.setId("1");
        portalInstallInfo1.setIp("127.0.0.1");
        portalInstallInfo1.setPort(22);
        portalInstallInfo1.setUser("test");
        portalInstallInfo1.setPortalType("MYSQL_ONLY");
        portalInstallInfo1.setInstallDir("/opt/portal");
        ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInstallInfo1);

        ArrayList<BaseInstallInfo> installInfoList = new ArrayList<>();
        installInfoList.add(portalInstallInfo1);
        String csvContent = CsvUtils.generateCsvContent(installInfoList);
        List<String> lines = CsvUtils.loadCsvLines(ThirdPartyToolEnum.MIGRATION_PORTAL.getInstallInfoCsvFilePath());
        Assert.assertEquals(csvContent, String.join(System.lineSeparator(), lines));

        ArrayList<ThirdPartyToolEnum> toolsList = new ArrayList<>();
        toolsList.add(ThirdPartyToolEnum.MIGRATION_PORTAL);
        csvContent = CsvUtils.generateCsvContent(toolsList);
        lines = CsvUtils.loadCsvLines(ThirdPartyToolEnum.THIRD_PARTY_TOOLS_CSV_FILE_PATH);
        Assert.assertEquals(csvContent, String.join(System.lineSeparator(), lines));
    }

    private static void assertSave() throws IOException {
        PortalInstallInfo portalInstallInfo2 = new PortalInstallInfo(
                "2", "127.0.0.1", 22, "test2", "/opt/portal2", "MYSQL_ONLY");
        ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInstallInfo2);

        List<String> lines = CsvUtils.loadCsvLines(ThirdPartyToolEnum.MIGRATION_PORTAL.getInstallInfoCsvFilePath());
        Assert.assertEquals(portalInstallInfo2.getCsvData(), lines.get(lines.size() - 1));
    }

    private static void assertDelete() throws IOException {
        PortalInstallInfo portalInstallInfo3 = new PortalInstallInfo(
                "id", "127.0.0.1", 22, "test3", "/opt/portal3", "MYSQL_ONLY");
        PortalInstallInfo portalInstallInfo4 = new PortalInstallInfo(
                "id4", "127.0.0.1", 22, "test4", "/opt/portal4", "MYSQL_ONLY");
        ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInstallInfo3);
        ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInstallInfo4);

        ThirdPartyToolManager.deleteById(ThirdPartyToolEnum.MIGRATION_PORTAL, "id");
        List<String> lines = CsvUtils.loadCsvLines(ThirdPartyToolEnum.MIGRATION_PORTAL.getInstallInfoCsvFilePath());
        Assert.assertEquals(portalInstallInfo4.getCsvData(), lines.get(lines.size() - 1));

        ThirdPartyToolManager.deleteById(ThirdPartyToolEnum.MIGRATION_PORTAL, "1");
        ThirdPartyToolManager.deleteById(ThirdPartyToolEnum.MIGRATION_PORTAL, "2");
        ThirdPartyToolManager.deleteById(ThirdPartyToolEnum.MIGRATION_PORTAL, "id4");
        lines = CsvUtils.loadCsvLines(ThirdPartyToolEnum.MIGRATION_PORTAL.getInstallInfoCsvFilePath());
        Assert.assertEquals(portalInstallInfo4.getCsvHeader(), lines.get(lines.size() - 1));
    }
}
