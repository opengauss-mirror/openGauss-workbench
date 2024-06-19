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
 *  PrometheusConfigDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/PrometheusConfigDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wuyuebin
 * @date 2023/4/27 17:21
 * @description alerting:
 * alertmanagers:
 * - static_configs:
 * - targets:
 * - alertmanager: 9093
 * global:
 * evaluation_interval: 15s
 * scrape_interval: 15s
 * rule_files: null
 * scrape_configs:
 * - job_name: prometheus
 * static_configs:
 * - targets:
 * - localhost:9090
 */
@Data
public class PrometheusConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Global global;
    private Alert alerting;
    @JsonProperty("rule_files")
    private List<String> ruleFiles;
    @JsonProperty("scrape_configs")
    private List<Job> scrapeConfigs;
    private List<RemoteRead> remote_read;

    @Data
    public static class Global implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("scrape_interval")
        private String scrapeInterval;
        @JsonProperty("evaluation_interval")
        private String evaluationInterval;
        @JsonProperty("scrape_timeout")
        private String scrapeTimeout;
    }

    @Data
    public static class Alert implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<Alertmanager> alertmanagers;

        @Data
        public static class Alertmanager implements Serializable {
            private static final long serialVersionUID = 1L;

            @JsonProperty("api_version")
            private String apiVersion;
            @JsonProperty("path_prefix")
            private String pathPrefix;
            private String scheme = "http";
            @JsonProperty("static_configs")
            private List<Conf> staticConfigs;
            @JsonProperty("follow_redirects")
            private Boolean isFollowRedirects;
            @JsonProperty("enable_http2")
            private Boolean isEnableHttp2;
            private String timeout;
            @JsonProperty("tls_config")
            private TlsConfig tlsConfig = new TlsConfig();

            @Data
            public static class Conf implements Serializable {
                private static final long serialVersionUID = 1L;

                private List<String> targets;
            }

            @Data
            public static class TlsConfig implements Serializable {
                private static final long serialVersionUID = 1L;
                @JsonProperty("insecure_skip_verify")
                private boolean insecureSkipVerify = true;
            }
        }
    }

    @Data
    public static class Job implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("job_name")
        private String jobName;
        @JsonProperty("static_configs")
        private List<Conf> staticConfigs;
        @JsonProperty("honor_timestamps")
        private Boolean isHonorTimestamps;
        @JsonProperty("scrape_interval")
        private String scrapeInterval;
        @JsonProperty("scrape_timeout")
        private String scrapeTimeout;
        @JsonProperty("metrics_path")
        private String metricsPath;
        private String scheme;
        @JsonProperty("follow_redirects")
        private Boolean isFollowRedirects;
        @JsonProperty("enable_http2")
        private Boolean isEnableHttp2;
        private Map params;

        @Data
        public static class Conf implements Serializable {
            private static final long serialVersionUID = 1L;

            private List<String> targets;
            private Map<String, String> labels;
        }

        @Override
        public int hashCode() {
            return jobName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Job)) {
                return false;
            }
            Job job = (Job) obj;
            return job.getJobName().equals(this.jobName);
        }
    }

    @Data
    public static class RemoteRead implements Serializable {
        private String url;
        private Boolean read_recent;
    }
}