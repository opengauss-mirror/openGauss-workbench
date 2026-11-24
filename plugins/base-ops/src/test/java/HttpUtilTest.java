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

import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.utils.http.HttpUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpUtilTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@Slf4j
public class HttpUtilTest {
    @Test
    void checkBaiduUrl() {
        AjaxResult baiduResult = HttpUtils.checkUrl("https://www.baidu.com");
        log.info("{}", baiduResult);
        assertTrue(baiduResult.isOk());
    }

    @Test
    void checkOpenGaussDownUrl() {
        AjaxResult openGaussDownUrlResult = HttpUtils.checkUrl(
            "https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86/openGauss-5.0.2-CentOS-64bit-all.tar.gz");
        log.info("{}", openGaussDownUrlResult);
        assertTrue(openGaussDownUrlResult.isOk());
    }

    @Test
    void checkOpenGaussDownErrorUrl() {
        AjaxResult openGaussDownUrlResult2 = HttpUtils.checkUrl(
            "https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.5/x86/openGauss-5.0.2-CentOS-64bit-all.tar.gz");
        log.info("{}", openGaussDownUrlResult2);
        assertFalse(openGaussDownUrlResult2.isOk());
    }

    @Test
    void checkOpenGaussHome() {
        AjaxResult openGaussDownUrlResult2 = HttpUtils.checkUrl("https://opengauss.org/zh/");
        log.info("{}", openGaussDownUrlResult2);
        assertTrue(openGaussDownUrlResult2.isOk());
    }
}
