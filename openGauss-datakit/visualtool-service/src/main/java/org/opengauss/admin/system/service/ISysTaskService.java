/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * ISysTaskService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysTaskService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.dto.SysTaskDto;
import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.system.domain.SysTask;

import java.util.List;
import java.util.Map;

/**
 * Task Service Interface
 *
 * @author xielibo
 */
public interface ISysTaskService extends IService<SysTask> {

    IPage<SysTask> selectList(IPage<SysTask> page, SysTaskDto task);

    List<SysTask> selectListAll(SysTaskDto task);

    Map<String, Object> getDetailById(Integer taskId);

    void deleteTask(Integer[] ids);

    void updateStatus(Integer id, SysTaskStatus taskStatus);

    void startTask(Integer id);

    void stopTask(Integer id);

    void refreshTaskStatus();
}
