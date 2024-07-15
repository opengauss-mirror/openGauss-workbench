/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertShieldingService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/AlertShieldingService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.AlertShieldingDTO;
import com.nctigba.alert.monitor.model.entity.AlertShieldingDO;
import com.nctigba.alert.monitor.model.query.AlertShieldingQuery;

/**
 * AlertShieldingService
 *
 * @author luomeng
 * @since 2024/6/30
 */
public interface AlertShieldingService extends IService<AlertShieldingDO> {
    /**
     * Select AlertShielding by page
     *
     * @param shieldingQuery AlertShieldingQuery
     * @param page           Page
     * @return Page<AlertShieldingDTO>
     */
    Page<AlertShieldingDTO> selectByPage(AlertShieldingQuery shieldingQuery, Page page);

    /**
     * Select AlertShielding by id
     *
     * @param id Long
     * @return AlertShieldingDO
     */
    AlertShieldingDO selectById(Long id);

    /**
     * Delete AlertShielding by id
     *
     * @param id Long
     */
    void deleteById(Long id);

    /**
     * Batch delete AlertShielding
     *
     * @param ids String
     */
    void batchDelete(String ids);

    /**
     * Update AlertShielding status by id
     *
     * @param id       Long
     * @param isEnable Integer
     */
    void updateStatusById(Long id, Integer isEnable);

    /**
     * Batch update AlertShielding status
     *
     * @param ids      String
     * @param isEnable Integer
     */
    void batchUpdateStatus(String ids, Integer isEnable);
}
