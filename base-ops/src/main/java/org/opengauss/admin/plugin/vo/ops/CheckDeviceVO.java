package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:36
 **/
@Data
public class CheckDeviceVO {
    private CheckItemVO checkBlockdev;
    private CheckItemVO checkDiskFormat;
    private CheckItemVO checkSpaceUsage;
    private CheckItemVO checkInodeUsage;
    private CheckItemVO checkSwapMemory;
    private CheckItemVO checkLogicalBlock;
    private CheckItemVO checkIOrequestqueue;
    private CheckItemVO checkMaxAsyIOrequests;
    private CheckItemVO checkIOConfigure;
}
