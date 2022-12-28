package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/11/30 08:56
 **/
@Data
@TableName("ops_encryption")
@EqualsAndHashCode(callSuper = true)
public class OpsEncryptionEntity extends BaseEntity {
    @TableId
    private String encryptionId;
    private String publicKey;
    private String privateKey;
}
