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
 *  ClusterStatisticsDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/cluster/ClusterStatisticsDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

import com.nctigba.observability.instance.enums.StateColor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClusterStatisticsDTO
 *
 * @author liupengfei
 * @since 2023/11/8
 */
@Data
public class ClusterStatisticsDTO {
    private PieChart cluster;
    private PieChart nodeStat;
    private PieChart nodeSyncStat;
    private List<BarChart> top5;

    /**
     * PieChart
     */
    @Data
    public static class PieChart {
        private Long total;
        private List<Proportion> proportion;
    }

    /**
     * Proportion
     */
    @Data
    public static class Proportion {
        private String value;
        private StateColor color;
        private Long num;
    }

    /**
     * BarChart
     */
    @Data
    public static class BarChart {
        private String nodeName;
        private String value;
        private Double tatio;
    }

    /**
     * build a PieChart
     *
     * @param list list to build a PieChart
     * @param classifier how to groupBy
     * @param allValues allValues
     * @param <T> T
     * @return PieChart
     */
    public static <T> PieChart buildPieChart(List<T> list, Function<T, StateDTO> classifier, String[] allValues) {
        ClusterStatisticsDTO.PieChart pieChart = new ClusterStatisticsDTO.PieChart();
        pieChart.setTotal((long) list.size());
        Map<StateDTO, Long> clusterStateGroup = list.stream().collect(Collectors
                .groupingBy(classifier, Collectors.counting()));
        Map<StateDTO, Long> counterMap = Arrays.stream(allValues).collect(Collectors
                .toMap(StateDTO::new, v -> 0L, (a, b) -> a));
        counterMap.putAll(clusterStateGroup);
        List<ClusterStatisticsDTO.Proportion> clusterProportion = counterMap.entrySet().stream().map(entry -> {
            ClusterStatisticsDTO.Proportion proportion = new ClusterStatisticsDTO.Proportion();
            proportion.setColor(entry.getKey().getColor());
            proportion.setValue(entry.getKey().getValue());
            proportion.setNum(entry.getValue());
            return proportion;
        }).collect(Collectors.toList());
        pieChart.setProportion(clusterProportion);
        return pieChart;
    }
}
