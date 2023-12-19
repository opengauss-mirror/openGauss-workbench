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
 *  TopSQLListQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/query/TopSQLListQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.query;

import java.sql.Timestamp;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * <p>
 * TopSQLList Request DTO
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 16:09
 */
@Data
public class TopSQLListQuery {
    private String id;
    private String startTime;
    private String finishTime;
    private String orderField;

    /**
     * getStartTimeTime
     *
     * @return Timestamp
     */
    public Timestamp getStartTimeTime() {
        return Timestamp.valueOf(getStartTime());
    }

    /**
     * getFinishTimeTime
     *
     * @return Timestamp
     */
    public Timestamp getFinishTimeTime() {
        return Timestamp.valueOf(getFinishTime());
    }

    /**
     * getOrderField
     *
     * @return String
     */
    public String getOrderField() {
        return StrUtil.isBlank(orderField) ? "execution_time" : orderField;
    }
}