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
 *  ColdFunction.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/ColdFunction.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.vo.FrameVO;
import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.TaskResultDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.OffCpuItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ColdFunction
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class ColdFunction implements DiagnosisPointService<Object> {
    @Autowired
    private OffCpuItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        option.add(String.valueOf(OptionEnum.IS_OFF_CPU));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object obj = dataStoreService.getData(item).getCollectionData();
        MultipartFile file = null;
        if (obj instanceof MultipartFile) {
            file = (MultipartFile) obj;
        }
        TableData table = getTableData(file);
        List<TaskResultDTO> list = getResultData(task, table);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!CollectionUtils.isEmpty(list)) {
            FrameVO f = new FrameVO();
            for (TaskResultDTO taskResultDTO : list) {
                f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
            }
            analysisDTO.setPointData(f);
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DISPLAY);
        return analysisDTO;
    }

    private TableData getTableData(MultipartFile file) {
        String[] keys = {"name", CommonConstants.SAMPLES, "ratio"};
        TableData table = new TableData(keys);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (line.contains("<title>")) {
                    String functionData = line.substring(line.indexOf("<title>") + 7, line.lastIndexOf("</title>"));
                    String functionName = functionData.substring(
                            0, functionData.lastIndexOf(CommonConstants.LEFT_BRACKET) - 1);
                    if ("all".equals(functionName)) {
                        continue;
                    }
                    String samples = functionData.substring(
                            functionData.lastIndexOf(CommonConstants.LEFT_BRACKET) + 1,
                            functionData.indexOf("samples,") - 1).replace(",", "");
                    String ratio = functionData.substring(
                            functionData.indexOf("samples,") + 9,
                            functionData.lastIndexOf(CommonConstants.RIGHT_BRACKET));
                    String[] datas = {functionName, samples, ratio};
                    var map = new HashMap<String, String>();
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        map.put(keys[i], datas[i]);
                    }
                    table.addData(map);
                }
            }
            return table;
        } catch (IOException e) {
            throw new CustomException("offCpu err", e);
        }
    }

    private List<TaskResultDTO> getResultData(DiagnosisTaskDO task, TableData table) {
        List<TaskResultDTO> list = new ArrayList<>();
        if (table.getData().size() > 1) {
            table.getData().sort((o1, o2) -> {
                Integer samples1 = Integer.valueOf(o1.get(CommonConstants.SAMPLES));
                Integer samples2 = Integer.valueOf(o2.get(CommonConstants.SAMPLES));
                return samples2.compareTo(samples1);
            });
            var it = table.getData().iterator();
            int count = 1;
            while (it.hasNext()) {
                it.next();
                if (count > 50) {
                    it.remove();
                }
                count++;
            }
            var center = new TaskResultDTO(
                    task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.ColdFunction, FrameTypeEnum.Table,
                    FrameVO.bearing.center);
            center.setData(table);
            TaskResultDTO taskResultDTO = new TaskResultDTO();
            taskResultDTO.setTaskId(task.getId());
            taskResultDTO.setResultType(ResultTypeEnum.ColdFunction);
            taskResultDTO.setFrameType(FrameTypeEnum.Suggestion);
            taskResultDTO.setState(TaskResultDTO.ResultState.SUGGESTION);
            taskResultDTO.setBearing(FrameVO.bearing.top);
            taskResultDTO.setData(
                    Map.of(CommonConstants.TITLE, LocaleStringUtils.format("ColdFunction.title"), "suggestions",
                            LocaleStringUtils.format("ColdFunction.name")));
            list.add(center);
        }
        return list;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }

    /**
     * TableData
     *
     * @author xx
     * @since 2023/6/9
     */
    @Data
    @NoArgsConstructor
    public static class TableData {
        private List<Map<String, String>> columns = new ArrayList<>();
        private List<Map<String, String>> data = new ArrayList<>();

        /**
         * Table data
         *
         * @param keys info
         */
        public TableData(String[] keys) {
            for (String key : keys) {
                columns.add(Map.of("key", key, CommonConstants.TITLE, key));
            }
        }

        /**
         * Add data
         *
         * @param map info
         */
        public void addData(Map<String, String> map) {
            data.add(map);
        }
    }
}