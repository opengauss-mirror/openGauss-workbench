/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.IndexAdvisorDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexAdvisor
 *
 * @author luomeng
 * @since 2023/8/17
 */
@Slf4j
@Service
public class IndexAdvisor implements HisDiagnosisPointService<Object> {
    @Autowired
    private ClusterManager clusterManager;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return null;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        ResultSet rs = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             Statement stmt = conn.createStatement()) {
            String diagnosisSql = task.getSql();
            rs = stmt.executeQuery("explain " + diagnosisSql);
            List<String> firstExplain = new ArrayList<>();
            while (rs.next()) {
                firstExplain.add(rs.getString(1));
            }
            String sql = "select \"schema\",\"table\", \"column\" ,\"indextype\" from gs_index_advise('"
                    + diagnosisSql.replace("'", "''") + "');";
            rs = stmt.executeQuery(sql);
            List<String> indexAdvisor = new ArrayList<>();
            while (rs.next()) {
                String schema = rs.getString(1);
                String tableName = rs.getString(2);
                String column = rs.getString(3);
                String indexType = rs.getString(4);
                if (column != null && !"".equals(column)) {
                    String createIndex;
                    if (indexType != null && !"".equals(indexType)) {
                        createIndex =
                                "create index " + tableName + "_" + System.currentTimeMillis() + " on " + schema + "."
                                        + tableName
                                        + " (" + column + ") " + indexType + ";";
                    } else {
                        createIndex =
                                "create index " + tableName + "_" + System.currentTimeMillis() + " on " + schema + "."
                                        + tableName + " (" + column + ");";
                    }
                    indexAdvisor.add(createIndex);
                }
            }
            List<Integer> virtualIndexId = new ArrayList<>();
            if (!CollectionUtils.isEmpty(indexAdvisor)) {
                for (String index : indexAdvisor) {
                    String createVirtualIndex = "select * from hypopg_create_index('" + index + "');";
                    rs = stmt.executeQuery(createVirtualIndex);
                    while (rs.next()) {
                        virtualIndexId.add(rs.getInt(1));
                    }
                }
            }
            List<String> afterExplain = new ArrayList<>();
            if (!CollectionUtils.isEmpty(indexAdvisor)) {
                String tunOnParam = "set enable_hypo_index = on;";
                stmt.execute(tunOnParam);
                String afterExplainSql = "explain " + diagnosisSql;
                rs = stmt.executeQuery(afterExplainSql);
                while (rs.next()) {
                    afterExplain.add(rs.getString(1));
                }
                String tunOffParam = "set enable_hypo_index = off;";
                stmt.execute(tunOffParam);
            }
            if (!CollectionUtils.isEmpty(virtualIndexId)) {
                for (Integer id : virtualIndexId) {
                    String createVirtualIndex = "select * from hypopg_drop_index(" + id + ");";
                    stmt.executeQuery(createVirtualIndex);
                }
            }
            IndexAdvisorDTO advisorDTO = new IndexAdvisorDTO();
            advisorDTO.setFirstExplain(firstExplain);
            advisorDTO.setIndexAdvisor(indexAdvisor);
            advisorDTO.setAfterExplain(afterExplain);
            AnalysisDTO analysisDTO = new AnalysisDTO();
            analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            analysisDTO.setPointData(advisorDTO);
            return analysisDTO;
        } catch (SQLException e) {
            throw new HisDiagnosisException("execute sql failed!");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error(CommonConstants.DATA_FAIL, e.getMessage());
                }
            }
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
