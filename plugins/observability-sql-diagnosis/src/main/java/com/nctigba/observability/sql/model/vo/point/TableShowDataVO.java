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
 *  TableShowDataVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/point/TableShowDataVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo.point;

import com.nctigba.observability.sql.enums.ShowDataEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * TableShowDataVO
 *
 * @author luomeng
 * @since 2023/11/22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TableShowDataVO extends ShowDataVO implements ShowData<Object> {
    private Object data;
    private List<TableShowDataColumnVO> columns;

    @Override
    public ShowDataEnum getType() {
        return ShowDataEnum.TABLE;
    }

    @Override
    public Object getData() {
        return data;
    }
}
