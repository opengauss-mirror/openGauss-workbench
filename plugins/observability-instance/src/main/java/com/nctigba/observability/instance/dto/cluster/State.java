/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import com.nctigba.observability.instance.constants.StateColor;
import com.nctigba.observability.instance.util.MessageSourceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * State
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class State {
    private String value;
    private StateColor color;

    State(String value) {
        this.value = value;
        setColor();
    }

    public void setColor() {
        this.color = StateColor.getColor(value);
    }

    public String getValue() {
        return MessageSourceUtil.getMsg(value);
    }
}
