package org.opengauss.admin.common.core.domain.model.ops.check;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/21 18:35
 **/
@Data
public class CheckOSVO {
    private CheckItemVO checkEncoding;
    private CheckItemVO checkFirewall;
    private CheckItemVO checkKernelVer;
    private CheckItemVO checkMaxHandle;
    private CheckItemVO checkNTPD;
    private CheckItemVO checkOSVer;
    private CheckItemVO checkSysParams;
    private CheckItemVO checkTHP;
    private CheckItemVO checkTimeZone;
    private CheckItemVO checkCPU;
    private CheckItemVO checkSshdService;
    private CheckItemVO checkSshdConfig;
    private CheckItemVO checkCrondService;
    private CheckItemVO checkStack;
    private CheckItemVO checkSysPortRange;
    private CheckItemVO checkMemInfo;
    private CheckItemVO checkHyperThread;
    private CheckItemVO checkMaxProcMemory;
    private CheckItemVO checkBootItems;
    private CheckItemVO checkKeyProAdj;
    private CheckItemVO checkFilehandle;
    private CheckItemVO checkDropCache;
}
