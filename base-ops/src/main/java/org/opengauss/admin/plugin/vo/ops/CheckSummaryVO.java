package org.opengauss.admin.plugin.vo.ops;

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
