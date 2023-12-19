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
 *  ParamShowDataVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/param/ParamShowDataVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo.param;

import com.nctigba.observability.sql.enums.ShowDataEnum;
import com.nctigba.observability.sql.model.dto.ParamDTO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.model.vo.point.ShowDataVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ParamShowDataVO
 *
 * @author luomeng
 * @since 2023/11/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ParamShowDataVO extends ShowDataVO implements ShowData<Object> {
    private ParamDTO data;

    @Override
    public ShowDataEnum getType() {
        return ShowDataEnum.PARAM;
    }

    @Override
    public Object getData() {
        return data;
    }
}
