/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
public class PrometheusConfigDto {
    private Global global;
    private Alert alerting;
    @JsonProperty("rule_files")
    private List<String> ruleFiles;
    @JsonProperty("scrape_configs")
    private List<Job> scrapeConfigs;

    @Data
    public static class Global {
        @JsonProperty("scrape_interval")
        private String scrapeInterval;
        @JsonProperty("evaluation_interval")
        private String evaluationInterval;
        @JsonProperty("scrape_timeout")
        private String scrapeTimeout;
    }

    @Data
    public static class Alert {
        private List<Alertmanager> alertmanagers;

        @Data
        public static class Alertmanager {
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

            @Data
            public static class Conf {
                private List<String> targets;
            }
        }
    }

    @Data
    public static class Job {
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

        @Data
        public static class Conf {
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
}
