/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.Generated;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 01:28
 * @description
 */
@Data
@Generated
public class AlertRuleConfigDto {
    private List<Group> groups;

    @Data
@Generated
    public static class Group {
        private String name;
        private List<Rule> rules;

        @Data
@Generated
        public static class Rule {
            private String alert;
            private String expr;
            @JsonProperty("for")
            private String aliasFor;
            private Labels labels;
            private Annotations annotations;

            @Data
@Generated
            public static class Labels {
                private String level;
                @JsonSerialize(using = ToStringSerializer.class)
                private Long templateId;
                @JsonSerialize(using = ToStringSerializer.class)
                private Long templateRuleId;
            }

            @Data
@Generated
            public static class Annotations {
                private String summary;
                private String description;
            }
        }
    }
}
