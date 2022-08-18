package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author lhf
 * @date 2022/10/13 15:03
 **/
@Data
@TableName("ops_wdr")
@EqualsAndHashCode(callSuper = true)
public class OpsWdrEntity extends BaseEntity {
    @TableId
    private String wdrId;
    private WdrScopeEnum scope;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportAt;
    private WdrTypeEnum reportType;
    private String reportName;
    private String reportPath;
    private String clusterId;
    private String nodeId;
    private String hostId;
    private String userId;
    private String startSnapshotId;
    private String endSnapshotId;
}
