/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.opengauss.utils.EncryptionUtils;
import org.opengauss.utils.ExcelUtils;
import org.opengauss.utils.FileUtils;
import org.opengauss.visualtool.api.model.ops.Host;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * HostController test
 *
 * @since 2024/10/30
 */
public class HostControllerTest {
    private static final Logger logger = LogManager.getLogger(HostControllerTest.class);

    private Host host;
    private String hostName;
    private String hostRemark;

    private String currentLocale = "zh-CN";
    private String uuid;
    private final File xlsxFile = new File("src/test/resources/file/ops/模板.xlsx");

    @Test
    public void setTestBasePath() {
        EncryptionUtils.getEncryptionKey();
        RestAssured.basePath = "/host";
        logger.info("HostControllerTest start.");
    }

    @Test(priority = 1)
    public void pingTest() {
        generateHostBody();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .when()
                .post("/ping")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    private void generateHostBody() {
        String hostNameModel = "host-%s:%d";
        hostName = String.format(Locale.ROOT, hostNameModel,
                AppConfigLoader.getAppConfig().getOpsHost().getPublicIp(),
                AppConfigLoader.getAppConfig().getOpsHost().getPort());

        host = Host.builder()
                .name(hostName)
                .privateIp(AppConfigLoader.getAppConfig().getOpsHost().getPrivateIp())
                .publicIp(AppConfigLoader.getAppConfig().getOpsHost().getPublicIp())
                .port(AppConfigLoader.getAppConfig().getOpsHost().getPort())
                .username("root")
                .password(EncryptionUtils.encrypt(AppConfigLoader.getAppConfig().getOpsHost().getPassword()))
                .isRemember(true)
                .build();
    }

    @Test(dependsOnMethods = "pingTest")
    public void addTest() {
        add();
    }

    private void add() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void pageTest() {
        page();
    }

    private void page() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(hostName));

        host.setHostId(response.jsonPath().getString("rows.find { it.name == '" + hostName + "' }.hostId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void pingHostIdTest() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .get("/ping/{hostId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "pingHostIdTest")
    public void editTest() {
        hostRemark = hostName + " host remark";
        host.setRemark(hostRemark);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(host)
                .pathParam("hostId", host.getHostId())
                .when()
                .put("/{hostId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.remark", Matchers.hasItem(hostRemark));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void delTest() {
        delete();
    }

    private void delete() {
        getRequestSpecification()
                .pathParam("hostId", host.getHostId())
                .when()
                .delete("/{hostId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "delTest")
    public void downloadTemplateTest() {
        downloadTemplate();
    }

    private void downloadTemplate() {
        Response response = getRequestSpecification()
                .pathParam("currentLocale", currentLocale)
                .when()
                .get("/downloadTemplate/{currentLocale}");

        response.then()
                .statusCode(200)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8")
                .header("Content-Length", Matchers.not(Matchers.equalTo("0")));

        try (InputStream inputStream = response.asInputStream()) {
            FileUtils.writeToFile(inputStream, xlsxFile.getPath());
        } catch (IOException e) {
            throw new ApiTestException("Write 模板.xlsx failed. ", e);
        }
    }

    @Test(dependsOnMethods = "downloadTemplateTest")
    public void uploadTest() {
        List<Object> hostRecord = getHostRecord();
        ExcelUtils.addRecordToExcel(xlsxFile.getPath(), hostRecord);

        upload();
    }

    private List<Object> getHostRecord() {
        return Arrays.asList(
                1,
                hostName,
                AppConfigLoader.getAppConfig().getOpsHost().getPrivateIp(),
                AppConfigLoader.getAppConfig().getOpsHost().getPublicIp(),
                AppConfigLoader.getAppConfig().getOpsHost().getPort(),
                "root",
                AppConfigLoader.getAppConfig().getOpsHost().getPassword(),
                "是"
        );
    }

    private void upload() {
        Response response = getRequestSpecification()
                .multiPart("file", xlsxFile)
                .when()
                .post("/upload");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("msg", Matchers.notNullValue());

        uuid = response.jsonPath().getString("msg");
    }

    @Test(dependsOnMethods = "uploadTest")
    public void invokeFileTest() {
        invokeFile(0);
    }

    private void invokeFile(int isInvoke) {
        getRequestSpecification()
                .param("uuid", uuid)
                .param("isInvoke", isInvoke)
                .param("currentLocale", currentLocale)
                .when()
                .post("/invokeFile")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "invokeFileTest")
    public void getImportPlanByUuidTest() {
        int maxRetries = 30;
        int retryCount = 0;
        while (retryCount++ < maxRetries) {
            Response response = getImportPlanByUuid();
            boolean isEnd = response.jsonPath().getBoolean("data.end");
            int successSum = response.jsonPath().getInt("data.successSum");
            int errorSum = response.jsonPath().getInt("data.errorSum");

            if (isEnd && successSum == 1) {
                break;
            } else if (errorSum == 1) {
                throw new ApiTestException("Failed to import host.");
            } else if (retryCount == maxRetries) {
                throw new ApiTestException("Importing host timed out.");
            } else {
                logger.debug("Host importing...");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new ApiTestException(e);
            }
        }

        invokeFile(1);
        page();
        delete();
    }

    private Response getImportPlanByUuid() {
        Response response = getRequestSpecification()
                .param("uuid", uuid)
                .when()
                .get("/get_import_plan");

        response.then()
                .body("code", Matchers.equalTo(200));

        return response;
    }

    @Test(dependsOnMethods = "getImportPlanByUuidTest")
    public void downloadErrorExcelTest() {
        downloadTemplate();

        List<Object> hostRecord = getHostRecord();
        hostRecord.set(6, "");
        ExcelUtils.addRecordToExcel(xlsxFile.getPath(), hostRecord);

        upload();
        invokeFile(0);

        int maxRetries = 60;
        int retryCount = 0;
        while (retryCount++ < maxRetries) {
            Response response = getImportPlanByUuid();
            boolean isEnd = response.jsonPath().getBoolean("data.end");
            int successSum = response.jsonPath().getInt("data.successSum");
            int errorSum = response.jsonPath().getInt("data.errorSum");

            if (isEnd && errorSum == 1) {
                break;
            } else if (successSum == 1) {
                throw new ApiTestException("An unexpected host import success occurred.");
            } else if (retryCount == maxRetries) {
                throw new ApiTestException("Importing host timed out.");
            } else {
                logger.debug("Host importing...");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new ApiTestException(e);
            }
        }

        getRequestSpecification()
                .pathParam("uuid", uuid)
                .when()
                .head("/downloadErrorExcel/{uuid}")
                .then()
                .statusCode(200)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8")
                .header("Content-Length", Matchers.not(Matchers.equalTo("0")));

        invokeFile(1);
        FileUtils.deleteFile(xlsxFile.getPath());
    }

    @Test(priority = 1)
    public void listSupportOsNameTest() {
        getRequestSpecification()
                .when()
                .get("/listSupportOsName")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    /**
     * add ops host
     *
     * @return ops Host
     */
    public Host addHost() {
        EncryptionUtils.getEncryptionKey();
        RestAssured.basePath = "/host";
        generateHostBody();
        add();
        page();
        return host;
    }

    /**
     * delete ops host
     */
    public void deleteHost() {
        RestAssured.basePath = "/host";
        delete();
    }
}
