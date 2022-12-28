package org.opengauss.admin.plugin.vo.ops;

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
