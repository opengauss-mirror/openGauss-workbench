package org.opengauss.admin.common.core.domain.model.ops.check;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:38
 **/
@Data
public class CheckItemVO {
    private String name;
    private String status;
    private String msg;
}
