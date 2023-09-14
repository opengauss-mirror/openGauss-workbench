/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.result;

import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HisTreeNode
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class HisTreeNode {
    private String title;
    private String pointName;
    private HisDiagnosisResult.PointType pointType;
    private List<HisTreeNode> child;
    private HisDiagnosisResult.PointState state;
    private Boolean isHidden;

    public HisTreeNode(
            String title, String pointName, HisDiagnosisResult.PointType pointType,
            HisDiagnosisResult.PointState state, Boolean isHidden) {
        this.title = title;
        this.pointName = pointName;
        this.pointType = pointType;
        this.state = state;
        this.isHidden = isHidden;
    }

    public HisTreeNode setState(HisDiagnosisResult.PointState state) {
        if (this.state == HisDiagnosisResult.PointState.SUCCEED) {
            return this;
        }
        this.state = state;
        return this;
    }

    public Boolean getIsHidden() {
        if (this.isHidden != null) {
            return this.isHidden;
        }
        if (this.state == HisDiagnosisResult.PointState.SUCCEED) {
            return this.isHidden = false;
        }
        List<HisDiagnosisResult.PointState> list = Arrays.asList(
                HisDiagnosisResult.PointState.NOT_MATCH_OPTION,
                HisDiagnosisResult.PointState.ANALYSIS_EXCEPTION,
                HisDiagnosisResult.PointState.COLLECT_EXCEPTION,
                HisDiagnosisResult.PointState.NOT_SATISFIED_DIAGNOSIS,
                HisDiagnosisResult.PointState.NOT_HAVE_DATA
        );
        boolean isState = list.contains(state);
        if (CollectionUtils.isEmpty(child) && isState) {
            return this.isHidden = true;
        }
        if (!CollectionUtils.isEmpty(child)) {
            for (HisTreeNode treeNode : child) {
                if (!treeNode.getIsHidden()) {
                    return this.isHidden = false;
                }
            }
        }
        if (pointName != null) {
            return this.isHidden = false;
        }
        return this.isHidden = true;
    }

    public HisTreeNode appendChild(HisTreeNode node) {
        if (child == null) {
            child = new ArrayList<>();
        }
        child.add(node);
        return this;
    }
}
