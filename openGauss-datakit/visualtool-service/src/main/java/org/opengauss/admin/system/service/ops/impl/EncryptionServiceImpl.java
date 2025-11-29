/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * EncryptionServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.crypto.asymmetric.KeyType;

import org.opengauss.admin.common.core.domain.entity.ops.OpsEncryptionEntity;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.mapper.ops.OpsEncryptionMapper;
import org.opengauss.admin.system.service.ops.IEncryptionService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author lhf
 * @date 2022/12/2 18:37
 **/
@Service
public class EncryptionServiceImpl extends ServiceImpl<OpsEncryptionMapper, OpsEncryptionEntity>
    implements IEncryptionService {
    @Override
    public String getEncryptedKey(String keyIdentifier) {
        OpsEncryptionEntity keyEntry = getById(1);
        if (Objects.isNull(keyEntry)) {
            throw new CustomException("encryption key not found");
        }
        return KeyType.PrivateKey.equals(KeyType.valueOf(keyIdentifier))
            ? keyEntry.getPrivateKey()
            : keyEntry.getPublicKey();
    }
}
