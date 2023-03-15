package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.common.core.domain.BaseEntity;

/**
 * @author lhf
 * @date 2023/3/14 23:34
 **/
@Data
@TableName("ops_host_tag_rel")
@EqualsAndHashCode(callSuper = true)
public class OpsHostTagRel extends BaseEntity {
    @TableId
    private String id;
    private String hostId;
    private String tagId;
}
