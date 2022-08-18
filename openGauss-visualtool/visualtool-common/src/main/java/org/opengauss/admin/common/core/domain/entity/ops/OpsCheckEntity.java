package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/11/13 17:15
 **/
@Data
@TableName("ops_check")
@EqualsAndHashCode(callSuper = true)
public class OpsCheckEntity extends BaseEntity {
    private String checkId;
    private String clusterId;
    private String checkRes;
}
