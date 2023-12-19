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
 *  DatabaseParam.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/DatabaseParam.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.enums.DatabaseParamEnum;
import com.nctigba.observability.sql.enums.OptionEnum;
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
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.table.DatabaseItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseParam
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
@Slf4j
public class DatabaseParam implements DiagnosisPointService<Object> {
    @Autowired
    private DatabaseItem item;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DiagnosisResultMapper resultMapper;
    @Autowired
    private ParamInfoMapper paramInfoMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
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
        ResultSet rs = null;
        try (
                var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
                Statement stmt = conn.createStatement()) {
            String sql = SqlConstants.DATABASE_ALL_PARAM;
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString(1);
                DatabaseParamEnum[] fields = DatabaseParamEnum.values();
                String nodeName = null;
                for (DatabaseParamEnum field : fields) {
                    if (field.getParamName().equals(name)) {
                        nodeName = field.toString();
                    }
                }
                if (nodeName == null) {
                    continue;
                }
                AnalysisDTO analysisDTO = new AnalysisDTO();
                analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
                ParamDTO paramDto = new ParamDTO();
                DiagnosisResultDO.PointState pointState;
                ParamInfoDO paramInfo = paramInfoMapper.selectOne(
                        Wrappers.lambdaQuery(ParamInfoDO.class).eq(ParamInfoDO::getParamName, name));
                String value = rs.getString(2);
                String pointName = null;
                if (paramInfo != null) {
                    DatabaseParamEnum[] dbFields = DatabaseParamEnum.values();
                    for (DatabaseParamEnum dbField : dbFields) {
                        if (dbField.getParamName().equals(paramInfo.getParamName())) {
                            pointName = dbField.toString();
                        }
                    }
                    paramDto.setParamName(paramInfo.getParamName());
                    paramDto.setCurrentValue(value);
                    paramDto.setUnit(paramInfo.getUnit());
                    paramDto.setParamDescription(paramInfo.getParamDetail());
                    paramDto.setSuggestValue(paramInfo.getSuggestValue());
                    paramDto.setSuggestReason(paramInfo.getSuggestExplain());
                    if (paramInfo.getDiagnosisRule() != null || !"".equals(
                            paramInfo.getDiagnosisRule())) {
                        var manager = new ScriptEngineManager();
                        var t = manager.getEngineByName("javascript");
                        var bindings = t.createBindings();
                        bindings.put("ACTUALVALUE", value);
                        Object object = t.eval(paramInfo.getDiagnosisRule(), bindings);
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
                analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
                DiagnosisResultDO resultData = new DiagnosisResultDO(
                        task, analysisDTO, pointName, pointState);
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
        } catch (SQLException | ScriptException e) {
            task.addRemarks("get param info failed", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error(CommonConstants.DATA_FAIL, e.getMessage());
                }
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        DiagnosisResultDO resultData = new DiagnosisResultDO(
                task, analysisDTO, "DatabaseParam", DiagnosisResultDO.PointState.SUCCEED);
        resultMapper.update(resultData, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                        DiagnosisResultDO::getTaskId, resultData.getTaskId())
                .eq(DiagnosisResultDO::getPointName, resultData.getPointName()));
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
