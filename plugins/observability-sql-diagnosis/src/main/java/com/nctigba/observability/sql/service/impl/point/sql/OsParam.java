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
 *  OsParam.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/OsParam.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.OsParamEnum;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.param.ParamInfoMapper;
import com.nctigba.observability.sql.model.dto.ParamDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.ParamInfoDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.param.ParamShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.OsParamItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * OsParam
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class OsParam implements DiagnosisPointService<Object> {
    @Autowired
    private OsParamItem item;
    @Autowired
    private DiagnosisResultMapper resultMapper;
    @Autowired
    private ParamInfoMapper paramInfoMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        option.add(String.valueOf(OptionEnum.IS_PARAM));
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
        HashMap<String, String> paramMap = fileToMap(file);
        try {
            List<ParamInfoDO> paramInfoDOS = paramInfoMapper.selectList(
                    Wrappers.lambdaQuery(ParamInfoDO.class).eq(ParamInfoDO::getParamType, "OS"));
            for (ParamInfoDO paramInfoDO : paramInfoDOS) {
                String paramName = paramInfoDO.getParamName();
                OsParamEnum[] fields = OsParamEnum.values();
                String nodeName = null;
                for (OsParamEnum field : fields) {
                    if (field.getParamName().equals(paramName)) {
                        nodeName = field.toString();
                    }
                }
                AnalysisDTO analysisDTO = new AnalysisDTO();
                analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
                ParamDTO paramDto = new ParamDTO();
                DiagnosisResultDO.PointState pointState;
                if (paramMap.containsKey(paramName)) {
                    String paramActualData = paramMap.get(paramName);
                    String paramData = paramActualData.replace("\t", "");
                    paramDto.setParamName(paramInfoDO.getParamName());
                    paramDto.setCurrentValue(paramActualData);
                    paramDto.setUnit(paramInfoDO.getUnit());
                    paramDto.setParamDescription(paramInfoDO.getParamDetail());
                    paramDto.setSuggestValue(paramInfoDO.getSuggestValue());
                    paramDto.setSuggestReason(paramInfoDO.getSuggestExplain());
                    if (paramInfoDO.getDiagnosisRule() != null || !"".equals(
                            paramInfoDO.getDiagnosisRule())) {
                        var manager = new ScriptEngineManager();
                        var t = manager.getEngineByName("javascript");
                        var bindings = t.createBindings();
                        bindings.put("ACTUALVALUE", paramData.trim());
                        Object object = t.eval(paramInfoDO.getDiagnosisRule(), bindings);
                        if (object != null && "true".equals(object.toString())) {
                            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                        } else {
                            paramDto.setTitle(
                                    LocaleStringUtils.format("Param.revise") + LocaleStringUtils.format(
                                            nodeName + ".title")
                                            + LocaleStringUtils.format("Param.define"));
                            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                        }
                    } else {
                        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                    }
                    pointState = DiagnosisResultDO.PointState.SUCCEED;
                } else {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                    pointState = DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS;
                }
                DiagnosisResultDO resultData = new DiagnosisResultDO(
                        task, analysisDTO, nodeName, pointState);
                ParamShowDataVO paramShowDataVO = new ParamShowDataVO();
                paramShowDataVO.setData(paramDto);
                AutoShowDataVO dataVO = new AutoShowDataVO();
                List<ShowData> list = new ArrayList<>();
                list.add(paramShowDataVO);
                dataVO.setData(list);
                resultData.setData(dataVO);
                resultMapper.update(resultData, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                DiagnosisResultDO::getTaskId, resultData.getTaskId())
                        .eq(DiagnosisResultDO::getPointName, resultData.getPointName()));
            }
        } catch (ScriptException e) {
            throw new CustomException("sql error:", e);
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        DiagnosisResultDO resultData = new DiagnosisResultDO(
                task, analysisDTO, "OsParam", DiagnosisResultDO.PointState.SUCCEED);
        resultMapper.update(resultData, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                        DiagnosisResultDO::getTaskId, resultData.getTaskId())
                .eq(DiagnosisResultDO::getPointName, resultData.getPointName()));
        return analysisDTO;
    }

    private HashMap<String, String> fileToMap(MultipartFile file) {
        HashMap<String, String> paramMap = new HashMap<>();
        Charset cs = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), cs))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String name = line.substring(0, line.indexOf(CommonConstants.EQUAL)).trim();
                String paramActualData = line.substring(line.indexOf(CommonConstants.EQUAL) + 1);
                paramMap.put(name, paramActualData);
            }
            return paramMap;
        } catch (IOException e) {
            throw new CustomException("file parse error:", e);
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}