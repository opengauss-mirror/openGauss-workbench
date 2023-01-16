package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.common.core.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

/**
 * @author lhf
 * @date 2023/1/13 10:59
 **/
@Data
@TableName("ops_jdbcdb_cluster")
@EqualsAndHashCode(callSuper = true)
public class OpsJdbcDbClusterEntity extends BaseEntity {
    @TableId
    private String clusterId;
    private String name;
    private DbTypeEnum dbType;
    private DeployTypeEnum deployType;
}
