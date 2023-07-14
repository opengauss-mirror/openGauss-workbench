/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/13 16:18
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
public class AlertTemplateRuleDto extends AlertTemplateRule {
    private String ruleExpDesc;

    public void buildRuleExpDesc() {
        this.ruleExpDesc = this.getAlertRuleItemList().stream().map(item -> {
            List<String> paramVals =
                item.getItemParamList().stream().filter(item0 -> item0.getItemId().equals(item.getId())).map(
                    item0 -> item0.getParamValue()).collect(Collectors.toList());
            return "[" + item.getRuleMark() + "]:" + MessageSourceUtil.get(item.getRuleExpName())
                + (CollectionUtil.isNotEmpty(paramVals)
                ? "(" + paramVals.stream().collect(Collectors.joining(CommonConstants.DELIMITER)) + ")" : "") + " "
                + (item.getAction().equals("normal")
                ? MessageSourceUtil.get("rule.ruleItem.normalAction") + " " + item.getOperate() + item.getLimitValue()
                + (StrUtil.isNotBlank(item.getUnit()) ? item.getUnit() : "") : item.getAction().equals(
                "increase") ? MessageSourceUtil.get("rule.ruleItem.increaseAction") : MessageSourceUtil.get(
                "rule.ruleItem.decreaseAction"));
        }).collect(Collectors.joining("<br/>"));
    }
}
