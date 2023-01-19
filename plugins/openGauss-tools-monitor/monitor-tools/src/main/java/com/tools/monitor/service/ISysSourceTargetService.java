package com.tools.monitor.service;

import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.entity.SysSourceTarget;

/**
 * ISysSourceTargetService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface ISysSourceTargetService {

    AjaxResult selectAll();

    AjaxResult save(SysSourceTarget sysSourceTarget);

}
