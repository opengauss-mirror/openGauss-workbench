/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.PackageTypeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.service.ops.impl.OpsPackageManagerService;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * InstallPackageTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class InstallPackageTest {
    private static final int USER_ID = 1;

    @InjectMocks
    @Spy
    private IOpsPackageManagerService opsPackageManagerService = new OpsPackageManagerService();
    @Mock
    private SysSettingFacade sysSettingFacade;

    @Before
    public void setup() throws IOException, InterruptedException {
        when(sysSettingFacade.getSysSetting(USER_ID)).thenReturn(getSysSetting());
        doReturn(true).when(opsPackageManagerService).saveOrUpdate(any());
        doReturn(true).when(opsPackageManagerService).removeById(anyString());
        doReturn(true).when(opsPackageManagerService).update(any());
        doReturn(getPkg()).when(opsPackageManagerService).getById(any());
    }

    @Test
    public void testSavePackage() throws IOException {
        OpsPackageManagerEntity pkgEntity = new OpsPackageManagerEntity();
        File file = new File("upload/openGauss-5.1.0-CentOS-64bit-all.tar.gz");
        // 创建一个FileInputStream对象，用于读取文件内容
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", fis);
        pkgEntity.setFile(multipartFile);
        pkgEntity.setName("TestPkgName");
        opsPackageManagerService.savePackage(pkgEntity, USER_ID);
    }

    @Test
    public void testDelPackage() throws IOException {
        opsPackageManagerService.delPackage("1");
    }

    @Test
    public void testGetCpuArchByPackagePath() throws IOException {
        String result = opsPackageManagerService.getCpuArchByPackagePath(
            "/upload/openGauss-Lite-5.1.1-CentOS-x86_64.tar.gz", OpenGaussVersionEnum.LITE);
        Assert.assertEquals(result, "x86_64");
    }

    @Test
    public void testUpdatePackage() throws IOException {
        opsPackageManagerService.updatePackage(getPkg(), 1);
    }

    @Test
    public void testAnalysisPkg() {
        OpsPackageVO result = opsPackageManagerService.analysisPkg("openGauss-Lite-5.0.1-CentOS-x86_64.tar.gz",
            PackageTypeEnum.OPENGAUSS.getPackageType());
        Assert.assertEquals(result.getOs(), "centos");
        Assert.assertEquals(result.getCpuArch(), "x86_64");
        Assert.assertEquals(result.getPackageVersion(), "LITE");
        Assert.assertEquals(result.getPackageVersionNum(), "5.0.1");
    }

    @Test
    public void testDeletePkgTar() {
        Assert.assertTrue(opsPackageManagerService.deletePkgTar("upload/openGauss-5.0.1-CentOS-64bit.tar.bz2", "1"));
    }

    @Test
    public void testGetSysUploadPath() {
        String result = opsPackageManagerService.getSysUploadPath(1);
        Assert.assertEquals(result, "upload");
    }

    private SysSettingEntity getSysSetting() {
        SysSettingEntity sysSetting = new SysSettingEntity();
        sysSetting.setId(1);
        sysSetting.setUserId(1);
        sysSetting.setUploadPath("upload");
        return sysSetting;
    }

    private OpsPackageManagerEntity getPkg() {
        OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
        entity.setPackageId("1");
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setName("TestPkgName");
        uploadInfo.setRealPath("upload/1.txt");
        entity.setPackagePath(uploadInfo);
        return entity;
    }
}
