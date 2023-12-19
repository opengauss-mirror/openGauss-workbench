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
 *  TreeNodeDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/TreeNodeDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto;

import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TreeNodeDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TreeNodeDTO {
    private String title;
    private String pointName;
    private DiagnosisResultDO.PointType pointType;
    private List<TreeNodeDTO> child;
    private DiagnosisResultDO.PointState state;
    private Boolean isHidden;

    public TreeNodeDTO(
            String title, String pointName, DiagnosisResultDO.PointType pointType,
            DiagnosisResultDO.PointState state, Boolean isHidden) {
        this.title = title;
        this.pointName = pointName;
        this.pointType = pointType;
        this.state = state;
        this.isHidden = isHidden;
    }

    public TreeNodeDTO setState(DiagnosisResultDO.PointState state) {
        if (this.state == DiagnosisResultDO.PointState.SUCCEED) {
            return this;
        }
        this.state = state;
        return this;
    }

    public Boolean getIsHidden() {
        if (this.isHidden != null) {
            return this.isHidden;
        }
        if (this.state == DiagnosisResultDO.PointState.SUCCEED) {
            return this.isHidden = false;
        }
        List<DiagnosisResultDO.PointState> list = Arrays.asList(
                DiagnosisResultDO.PointState.NOT_MATCH_OPTION,
                DiagnosisResultDO.PointState.ANALYSIS_EXCEPTION,
                DiagnosisResultDO.PointState.COLLECT_EXCEPTION,
                DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS,
                DiagnosisResultDO.PointState.NOT_HAVE_DATA
        );
        boolean isState = list.contains(state);
        if (CollectionUtils.isEmpty(child) && isState) {
            return this.isHidden = true;
        }
        if (!CollectionUtils.isEmpty(child)) {
            for (TreeNodeDTO treeNodeDTO : child) {
                if (!treeNodeDTO.getIsHidden()) {
                    return this.isHidden = false;
                }
            }
        }
        if (pointName != null) {
            return this.isHidden = false;
        }
        return this.isHidden = true;
    }

    public TreeNodeDTO appendChild(TreeNodeDTO node) {
        if (child == null) {
            child = new ArrayList<>();
        }
        child.add(node);
        return this;
    }
}
