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
 * CheckSummaryVO.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/model/ops/check/CheckSummaryVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.model.ops.check;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/11/13 17:22
 **/
@Data
public class CheckSummaryVO {
    private CheckVO checkVO;
    private Map<String, List<CheckItemVO>> summary;

    public static CheckSummaryVO of(CheckVO checkVO) {
        CheckSummaryVO checkSummaryVO = new CheckSummaryVO();
        if (Objects.nonNull(checkVO)) {
            checkSummaryVO.setCheckVO(checkVO);
            checkSummaryVO.setSummary(checkVO.summary());
        }
        return checkSummaryVO;
    }
}
