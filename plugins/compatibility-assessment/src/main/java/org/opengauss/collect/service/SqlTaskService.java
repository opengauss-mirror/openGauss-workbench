/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service;

import java.util.List;
import java.util.Optional;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.utils.response.RespBean;

/**
 * ISqlTaskService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface SqlTaskService {
    /**
     * getTaskList
     *
     * @param period period
     * @return RespBean
     */
    RespBean getTaskList(CollectPeriod period);

    /**
     * saveTask
     *
     * @param period period
     * @return RespBean RespBean
     */
    RespBean saveTask(CollectPeriod period);

    /**
     * updateTask
     *
     * @param period period
     * @return RespBean RespBean
     */
    RespBean updateTask(CollectPeriod period);

    /**
     * selectById
     *
     * @param id id
     * @return CollectPeriod CollectPeriod
     */
    Optional<CollectPeriod> selectById(Long id);

    /**
     * deleteTask
     *
     * @param ids ids
     * @return RespBean RespBean
     */
    RespBean deleteTask(List<Long> ids);

    /**
     * checkName
     *
     * @param name name
     * @return RespBean RespBean
     */
    RespBean checkName(String name);

    /**
     * getIps
     *
     * @return RespBean RespBean
     */
    RespBean getIps();

    /**
     * getCompleteProcess
     *
     * @return RespBean RespBean
     */
    RespBean getCompleteProcess();

    /**
     * getListByPid
     *
     * @param pid pid
     * @return List<CollectPeriod>
     */
    List<CollectPeriod> getListByPid(String pid);
}
