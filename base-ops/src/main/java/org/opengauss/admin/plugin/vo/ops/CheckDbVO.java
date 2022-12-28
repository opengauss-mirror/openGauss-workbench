package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:36
 **/
@Data
public class CheckDbVO {
    private CheckItemVO checkCurConnCount;
    private CheckItemVO checkCursorNum;
    private CheckItemVO checkPgxcgroup;
    private CheckItemVO checkTableSpace;
    private CheckItemVO checkSysadminUser;
    private CheckItemVO checkHashIndex;
    private CheckItemVO checkPgxcRedistb;
    private CheckItemVO checkNodeGroupName;
    private CheckItemVO checkTDDate;
}
