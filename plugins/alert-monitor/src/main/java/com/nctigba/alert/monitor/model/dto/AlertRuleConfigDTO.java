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
 *  AlertRuleConfigDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/AlertRuleConfigDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 01:28
 * @description
 */
@Data
public class AlertRuleConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Group> groups;

    @Data
    public static class Group implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private List<Rule> rules;

        @Data
        public static class Rule implements Serializable {
            private static final long serialVersionUID = 1L;

            private String alert;
            private String expr;
            @JsonProperty("for")
            private String aliasFor;
            private Labels labels;
            private Annotations annotations;

            @Data
            public static class Labels implements Serializable {
                private static final long serialVersionUID = 1L;

                private String level;
                @JsonSerialize(using = ToStringSerializer.class)
                private Long templateId;
                @JsonSerialize(using = ToStringSerializer.class)
                private Long templateRuleId;
            }

            @Data
            public static class Annotations implements Serializable {
                private static final long serialVersionUID = 1L;

                private String summary;
                private String description;
                private String value = "{{ $value }}";
            }
        }
    }
}