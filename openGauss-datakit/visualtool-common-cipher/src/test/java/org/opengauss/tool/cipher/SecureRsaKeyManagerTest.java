/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.cipher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * SecureRsaKeyManagerTest
 *
 * @author: wangchao
 * @Date: 2025/11/27 13:00
 * @since 7.0.0-RC3
 **/
public class SecureRsaKeyManagerTest {
    @Test
    public void testBasicOperations() throws Exception {
        RsaUtils keyManager = RsaUtils.getInstance();
        // 测试加密解密
        String originalText = "Hello, RSA Security!";
        String encrypted = keyManager.encrypt(originalText);
        String decrypted = keyManager.decrypt(encrypted);
        Assertions.assertEquals("加密解密后内容应该一致", originalText, decrypted);
        Assertions.assertNotEquals("加密后的内容应该不同", originalText, encrypted);
        // 测试签名验证
        String data = "重要数据需要签名";
        String signature = keyManager.sign(data);
        boolean isVerified = keyManager.verify(data, signature);
        Assertions.assertTrue(isVerified, "签名验证应该成功");
        // 测试篡改数据后验证失败
        boolean isTamperedVerified = keyManager.verify("被篡改的数据", signature);
        Assertions.assertFalse(isTamperedVerified, "篡改数据后验证应该失败");
    }

    @AfterEach
    public void tearDown() {
        // 清理所有RsaUtils实例
        RsaUtils.getInstance().clearKeyPair();
    }
}