/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2025.
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
 *  RsaUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/RsaUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import org.apache.commons.codec.binary.Base64;

/**
 * RsaUtils
 *
 * @author wuyuebin
 * @since 2025/7/29 16:58
 */
public class RsaUtils {
    /**
     * encrypt
     *
     * @param plainText String
     * @param publicKey String
     * @return String
     */
    public static String encrypt(String plainText, String publicKey) {
        RSA rsa = new RSA(null, publicKey);
        byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return StrUtil.str(Base64.encodeBase64(encrypt), CharsetUtil.CHARSET_UTF_8);
    }
}
