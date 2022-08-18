package org.opengauss.admin.framework.manager.factory;

import org.opengauss.admin.common.utils.ip.AddressUtils;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.admin.system.domain.SysOperLog;
import org.opengauss.admin.system.service.ISysOperLogService;

import java.util.Date;
import java.util.TimerTask;

/**
 * Async factory
 *
 * @author xielibo
 */
public class AsyncFactory {

    /**
     * Record Operation Log
     *
     * @param operLog operLog
     * @return task
     */
    public static TimerTask recordOper(final SysOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
            operLog.setOperLocation(AddressUtils.getRealAddressByIp(operLog.getOperIp()));
            operLog.setOperTime(new Date());
            SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
