package com.nctigba.observability.sql.service.diagnosis.caller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskResultMapper;
import com.nctigba.observability.sql.model.PartitionDataResp;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;
import com.nctigba.observability.sql.service.ClusterManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyzeCaller implements Caller {
//	private static final String TOP_SQL_LIST_SQL = "select debug_query_id from dbe_perf.statement_history where debug_query_id != 0 and query = 'QUERY_TEXT' order by finish_time desc limit 1;";
	private static final String QUERY_ALL_COLUMN_NAME = "select column_name from information_schema.columns where table_name = 'TABLENAME'";
	private static final String WORK_MEM_SQL = "select setting from pg_settings WHERE name = 'work_mem'";
	private static final String TABLE_METADATA_SQL = "select row_to_json(t) from (select schemaname,t1.relname,pg_relation_size(relid) object_size, relkind object_type,n_live_tup,n_dead_tup,\n"
			+ "case when n_live_tup+n_dead_tup=0 then '-' else round(n_dead_tup*100/(n_dead_tup+n_live_tup),2)||'%' end dead_tup_ratio,\n"
			+ "last_vacuum,last_autovacuum,last_analyze,last_autoanalyze\n" + "from pg_catalog.pg_stat_all_tables t1\n"
			+ "left join pg_catalog.pg_class t2 on t1.relid = t2.oid\n" + "where t1.relname = 'TABLENAME')t";
	private static final String INDEX_SQL = "select row_to_json(t) from (select c2.relname,i.indisprimary,i.indisunique,i.indisclustered,i.indisvalid,\n"
			+ "i.indisreplident,pg_catalog.pg_get_indexdef(i.indexrelid,0,true) as def\n"
			+ "from pg_catalog.pg_class c, pg_catalog.pg_class c2,pg_catalog.pg_index i\n"
			+ "where c.relname='TABLENAME' and c.oid=i.indrelid and c2.oid=i.indexrelid) t";
	private static final String TABLE_STRUCTURE_SQL = "select row_to_json(t) from (select a.attnum,a.attname,t.typname,a.attlen,a.attnotnull,b.description\n"
			+ "from pg_catalog.pg_class c,pg_catalog.pg_attribute a\n"
			+ "left outer join pg_catalog.pg_description b on a.attrelid=b.objoid and a.attnum=b.objsubid,pg_catalog.pg_type t\n"
			+ "where c.relname='TABLENAME' and a.attnum>0 and a.attrelid=c.oid and a.atttypid=t.oid\n"
			+ "order by a.attnum) t";
	private static final String PARTITION_LIST_SQL = "select row_to_json(t) from (select partstrategy, partkey, relpages, reltuples, relallvisible, interval from pg_partition WHERE parttype = 'r' and relname = 'TABLENAME') t";
	private static final String DEBUG_QUERY_CHECK = "show track_stmt_parameter";
	private static final String DEBUG_QUERY_ID_SQL = "select query_id from pg_stat_activity where sessionid = '%d' and state != 'idle' and query_id != 0";
	private static final String QUERY_TABLE_ALL_ROWS = "SELECT reltuples FROM pg_class WHERE relname = 'TABLENAME'";
	private final ClusterManager clusterManager;
	private final DiagnosisTaskMapper diagnosisTaskMapper;
	private final DiagnosisTaskResultMapper taskResultMapper;
	private final TopSQLHandler openGaussTopSQLHandler;

	@Override
	public void beforeStart(Task task) {
		log.info("analyze caller beforeStart begin");
		if (task.getConf() == null || !task.getConf().isExplainAnalysis())
			return;
		try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());) {
			try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(DEBUG_QUERY_CHECK);) {
				if (rs.next() && !rs.getString(1).equals("on")) {
					task.getConf().setExplainAnalysis(false);
					log.info("track_stmt_parameter off");
				}
			}
		} catch (Exception e) {
			log.info("DEBUG_QUERY_ID check fail: {}", e.getMessage());
			task.addRemarks("DEBUG_QUERY_ID check failer", e);
		}
	}

	@Override
	@Async
	public void start(Task task) {
		log.info("analyze caller start begin");
		task.addRemarks("catching DEBUG_QUERY_ID");
		diagnosisTaskMapper.updateById(task);
		try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());) {
			Long debugQueryId = null;
			int counter = 0;
			while (debugQueryId == null && counter++ < 1000 && task.running())
				try (var stmt = conn.createStatement();
					var rs = stmt.executeQuery(String.format(DEBUG_QUERY_ID_SQL, task.waitSessionId()));) {
					if (rs.next()) {
						debugQueryId = rs.getLong(1);
						log.info("DEBUG_QUERY_ID catch success: {}", debugQueryId);
					} else {
						log.info("DEBUG_QUERY_ID null");
						Thread.sleep(10L);
					}
				}
			task.getData().setDebugQueryId(debugQueryId);
			task.addRemarks("DEBUG_QUERY_ID:" + debugQueryId);
		} catch (Exception e) {
			log.info("DEBUG_QUERY_ID catch exception: {}", e.getMessage());
			task.addRemarks("DEBUG_QUERY_ID catch failer");
		} finally {
			diagnosisTaskMapper.updateById(task);
		}
	}

	@Override
	public void afterSql(Task task, ArrayList<String> rsList) {
		log.info("analyze caller afterSql begin");
		setTaskResultSuggestions(task.getId(), ResultType.ExecPlan, null);
		if (!task.getConf().isExplainAnalysis())
			return;
		JSONObject result = new JSONObject();
		log.info("analyze caller getExecutionPlanResult start");
		try {
			result.put("executionPlan", getExecutionPlanResult(rsList, task.getNodeId(), task));
		} catch (Exception e) {
			log.info("analyze caller getExecutionPlanResult exception");
		}
		// Get data uniformly with ObjectInfoCheck
		setTaskResultSuggestions(task.getId(), ResultType.ObjectInfoCheck, result);
	}

	private void setTaskResultSuggestions(Integer taskId, ResultType resultType, JSONObject result) {
		TaskResult taskResult = new TaskResult();
		taskResult.setTaskid(taskId);
		taskResult.setResultType(resultType);
		taskResult.setState(TaskResult.ResultState.Suggestions);
		taskResult.setFrameType(FrameType.Explain);
		if (result != null) {
			taskResult.setData(result);
		}
		taskResultMapper.insert(taskResult);
	}

	/**
	 * get native execution plan
	 *
	 * @param rsList execution plan list
	 * @param nodeId node id
	 * @return JSONObject
	 */
	public JSONObject getExecutionPlanResult(ArrayList<String> rsList, String nodeId, Task task) {
		JSONObject jsonResult = new JSONObject();
		// get execution analyze native query plan
		jsonResult.put("queryPlan", rsList);
		int peakMem = getPeakOrDiskMemory(rsList);
		// get peak memory
		jsonResult.put("peakMem", peakMem);
		// max cost result
		JSONObject maxCostResult = getMaxCostTableName(rsList);
		String maxCostTableName = maxCostResult.getString("mostCostTableName");
		String maxCostStepName = maxCostResult.getString("mostCostStepName");
		jsonResult.put("maxCostTableName", maxCostTableName);
		jsonResult.put("maxCostStepName", maxCostStepName);
		// get connection
		try (var conn = clusterManager.getConnectionByNodeId(nodeId)) {
			// get rows diff list
			List<JSONObject> rowsDiffList = getRowsDiff(rsList, maxCostTableName, conn);
			jsonResult.put("rowsDiff", rowsDiffList);
			// suggest: ResultType.ObjectRecommendedToUpdateStatistics
			if (rowsDiffList != null && rowsDiffList.size() > 0) {
				log.info("get ObjectRecommendedToUpdateStatistics success");
				setTaskResultSuggestions(task.getId(), ResultType.ObjectRecommendedToUpdateStatistics, null);
			}
			// get work memory
			String workMem = getWorkMem(conn);
			jsonResult.put("workMem", workMem);
			// get debug_query_id
			jsonResult.put("debugQueryId", task.getData().getDebugQueryId());
			// get json query plan
			jsonResult.put("jsonQueryPlan", getJsonQueryPlan(task, rsList));
			try {
				// suggest: ResultType.PlanRecommendedToOptimizeStatementsOrAddWorkMemSize
				if (peakMem > Integer.parseInt(workMem)) {
					log.info("get PlanRecommendedToOptimizeStatementsOrAddWorkMemSize success");
					setTaskResultSuggestions(task.getId(),
							ResultType.PlanRecommendedToOptimizeStatementsOrAddWorkMemSize, null);
				}
			} catch (Exception e) {
				log.error("trans ResultType.PlanRecommendedToOptimizeStatementsOrAddWorkMemSize fail:{}",
						e.getMessage());
			}
			JSONObject partitionData = getPartitionData(task, rsList, maxCostTableName, conn);
			jsonResult.put("partitionData", JSON.parse(partitionData.getString("partitionResultArray")));
			boolean isHasPartKey = Boolean.parseBoolean(partitionData.getString("isHasPartKey"));
			boolean isByPartition = Boolean.parseBoolean(partitionData.getString("isByPartition"));
			jsonResult.put("isHasPartKey", isHasPartKey);

			// suggest: PlanRecommendedToQueryBasedOnPartition
			if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Partitioned Seq Scan") && !isByPartition && !isHasPartKey) {
				log.info("get PlanRecommendedToQueryBasedOnPartition success");
				setTaskResultSuggestions(task.getId(), ResultType.PlanRecommendedToQueryBasedOnPartition, null);
			}
			JSONObject tableMetaData = getTableData(maxCostTableName, conn, TABLE_METADATA_SQL, false);
			jsonResult.put("tableMetaData", tableMetaData);
			if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Seq Scan")) {
				try {
					JSONObject curTableMetaData = JSONObject.parseObject(tableMetaData.getString(maxCostTableName));
					int nLiveTup = Integer.parseInt(curTableMetaData.getString("n_live_tup"));
					int nDeadTup = Integer.parseInt(curTableMetaData.getString("n_dead_tup"));

					// suggest: ResultType.PlanChangedToPartitionTable
					if (nLiveTup > 2000 * 10000) {
						log.info("get PlanChangedToPartitionTable success");
						setTaskResultSuggestions(task.getId(), ResultType.PlanChangedToPartitionTable, null);
					}

					// suggest: ResultType.PlanRecommendedToDoVacuumCleaning
					if (nDeadTup > ((nLiveTup + nDeadTup) * 0.2 + 50)) {
						log.info("get PlanRecommendedToDoVacuumCleaning success");
						setTaskResultSuggestions(task.getId(), ResultType.PlanRecommendedToDoVacuumCleaning, null);
					}
				} catch (Exception e) {
					log.error("trans PlanChangedToPartitionTable or PlanRecommendedToDoVacuumCleaning fail:{}",
							e.getMessage());
				}
			}

			// get table structure data
			jsonResult.put("tableStructureData", getTableData(maxCostTableName, conn, TABLE_STRUCTURE_SQL, true));

			// this is table index info, not index advice
			JSONObject tableIndexData = getTableData(maxCostTableName, conn, INDEX_SQL, true);
			jsonResult.put("tableIndexData", tableIndexData);

			// get index advice
			String completeQueryText = "select * from gs_index_advise('"
					+ task.getSql().replace("\n", " ").replace("'", "''") + "')";
			JSONObject indexAdviceJsonResult = getIndexAdvice(conn, completeQueryText);
			JSONArray indexAdvicesData = indexAdviceJsonResult.getJSONArray("indexList");
			JSONArray indexAdvicesTableNameList = indexAdviceJsonResult.getJSONArray("tableNameList");
			jsonResult.put("indexAdvicesData", indexAdvicesData);
			jsonResult.put("indexAdvicesTableNameList", indexAdvicesTableNameList);
			List<JSONObject> otherStructureAndIndexList = new ArrayList<>();
			// suggest: ResultType.PlanRecommendedToCreateIndex
			if (StringUtils.isNotEmpty(maxCostStepName) && maxCostStepName.contains("Seq Scan")
					&& indexAdvicesData != null && !indexAdvicesData.isEmpty()) {
				log.info("get PlanRecommendedToCreateIndex success");
				setTaskResultSuggestions(task.getId(), ResultType.PlanRecommendedToCreateIndex, null);
				// get all index advice table's all
				log.info("get indexAdvicesTableNameList : {}", indexAdvicesTableNameList);
				for (Object name : indexAdvicesTableNameList) {
					String nameStr = String.valueOf(name);
					if (nameStr.equals(maxCostTableName)) {
						continue;
					}
					JSONObject structureData = getTableData(nameStr, conn, TABLE_STRUCTURE_SQL, true);
					JSONObject indexData = getTableData(nameStr, conn, INDEX_SQL, true);
					JSONObject item = new JSONObject();
					item.put("tableName", nameStr);
					item.put("structureData", structureData.getJSONArray(nameStr));
					item.put("indexData", indexData.getJSONArray(nameStr));
					otherStructureAndIndexList.add(item);
				}
			}
			jsonResult.put("otherStructureAndIndexList", otherStructureAndIndexList);
		} catch (Exception e) {
			log.error("get execution plan fail:{}", e.getMessage());
		}
		return jsonResult;
	}

	public JSONObject getIndexAdvice(Connection conn, String sql) {
		JSONObject jsonResult = new JSONObject();
		JSONArray indexList = new JSONArray();
		JSONArray tableNameList = new JSONArray();
		String indexTemplate = "It is recommended to create an index for column %c of table %t";
		String multiColumnIndexTemplate = "It is recommended to create a composite index for column %c of table %t";
		log.info("get index advice: {}", sql);
		try {
			var stmt = conn.createStatement();
			try (var rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					// 1: Schema ; 2: Table ; 3: Column; 4: IndexType
					String tableName = rs.getString(2);
					String columnStr = rs.getString(3);
					if (StringUtils.isNotEmpty(columnStr) && StringUtils.isNotEmpty(tableName)) {
						String result = columnStr.contains(",") ? multiColumnIndexTemplate : indexTemplate;
						result = result.replace("%t", tableName);
						result = result.replace("%c", columnStr);
						tableNameList.add(tableName);
						indexList.add(result);
					}
				}
				stmt.close();
				jsonResult.put("indexList", indexList);
				jsonResult.put("tableNameList", tableNameList);
				return jsonResult;
			}
		} catch (Exception e) {
			log.error("get index advice fail:{}", e.getMessage());
			return jsonResult;
		}
	}

	public JSONObject getTableData(String tableName, Connection conn, String sql, boolean isList) {
		JSONObject result = new JSONObject();
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}
		log.info("get table information tableName: {}, sql: {}", tableName, sql);
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(sql.replace("TABLENAME", tableName));
			if (isList) {
				JSONArray resultArray = new JSONArray();
				while (rs.next()) {
					resultArray.add(JSONObject.parseObject(rs.getString(1)));
				}
				result.put(tableName, resultArray);
			} else {
				while (rs.next()) {
					result.put(tableName, JSONObject.parseObject(rs.getString(1)));
				}
			}
			stmt.close();
			return result;
		} catch (Exception e) {
			log.error("get {} data fail:{}", sql, e.getMessage());
			return null;
		}
	}

	public JSONObject getJsonQueryPlan(Task task, ArrayList<String> rsList) {
		JSONObject result = new JSONObject();
		try {
			log.info("get Json Query Plan begin, debug query id : {}", task.getData().getDebugQueryId());
			if (rsList != null && !rsList.isEmpty()) {
				result = openGaussTopSQLHandler.dealExecutionPlan(rsList);
				log.info("Json Query Plan result : {}", result);
			}
		} catch (Exception e) {
			log.error("get json execution plan fail:{}", e.getMessage());
		}
		return result;
	}

	public JSONObject getMaxCostTableName(ArrayList<String> rsList) {
		JSONObject result = new JSONObject();
		try {
			LinkedList<String> modifyLines = new LinkedList<>(rsList);
			for (String rs : rsList) {
				if ((rs.contains("cost=") && !rs.contains("Result")) || rs.contains("Hash Cond") || rs.contains("->")) {
					continue;
				}
				modifyLines.remove(rs);
			}
			double cost = -1D;
			String mostCostTableName = "";
			String mostCostStepName = "";
			for (String mItem : modifyLines) {
				// avoid function, Partition has 'on'
				if (mItem.contains(" on ") && mItem.contains("..") && mItem.contains("rows")) {
					double tempCost = Double
							.parseDouble(mItem.substring(mItem.indexOf("..") + 2, mItem.indexOf("rows")).trim());
					if (tempCost > cost) {
						// get mostCostTableName
						String[] mItemArr = mItem.split(" on ");
						String text = mItemArr[1].trim();
						mostCostTableName = text.substring(0, text.indexOf(" "));
						// get mostCostStepNmae
						String[] stepArr = mItem.split(mostCostTableName);
						String step = stepArr[0].trim();
						if (step.contains("->")) {
							mostCostStepName = step.substring(step.indexOf("->") + 2) + " " + mostCostTableName;
						} else {
							mostCostStepName = step + " " + mostCostTableName;
						}
						cost = tempCost;
					}
				}
			}
			result.put("mostCostTableName", mostCostTableName);
			result.put("mostCostStepName", mostCostStepName.trim());
			log.info("get max cost table name success:{}, step name: {}", mostCostTableName, mostCostStepName);
			return result;
		} catch (Exception e) {
			log.error("get max cost table name fail:{}", e.getMessage());
			return result;
		}
	}

	public String getWorkMem(Connection conn) {
		String workMemValue = "";
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(WORK_MEM_SQL);
			while (rs.next()) {
				workMemValue = rs.getString(1);
			}
			stmt.close();
		} catch (Exception e) {
			log.error("get work memory fail:{}", e.getMessage());
		}
		return workMemValue;
	}

	public int getPeakOrDiskMemory(ArrayList<String> rsList) {
		if (rsList == null) {
			return -1;
		}
		int mostPeakMemory = -1;
		try {
			for (String rs : rsList) {
				if (rs.contains("Memory") || rs.contains("Disk")) {
					int lastSignPosi = rs.lastIndexOf(":");
					int lastUnitPosi = rs.lastIndexOf("kB");
					if (lastSignPosi > -1 && lastUnitPosi > -1 && (lastUnitPosi > lastSignPosi)) {
						int curPeakMemory = Integer.parseInt(rs.substring(lastSignPosi + 1, lastUnitPosi).trim());
						if (curPeakMemory > mostPeakMemory) {
							mostPeakMemory = curPeakMemory;
						}
					}
				}
			}
			return mostPeakMemory;
		} catch (Exception e) {
			log.error("get peak memory fail:{}", e.getMessage());
			return -1;
		}
	}

	public List<JSONObject> getRowsDiff(ArrayList<String> rsList, String tableName, Connection conn) {
		log.info("analyze caller getRowsDiff start");
		List<JSONObject> result = new ArrayList<>();
		int allRowsNum = -1;
		// get all rows
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(QUERY_TABLE_ALL_ROWS.replace("TABLENAME", tableName));
			while (rs.next()) {
				allRowsNum = rs.getInt(1);
			}
			stmt.close();
		} catch (Exception e) {
			log.error("get work memory fail:{}", e.getMessage());
			return result;
		}
		if (allRowsNum < 0) {
			return result;
		}
		// deal result
		try {
			LinkedList<String> modifyLines = new LinkedList<>(rsList);
			double diff = allRowsNum * 0.2 + 1000;
			for (String rs : rsList) {
				if ((rs.contains("cost=") && !rs.contains("Result")) || rs.contains("Hash Cond") || rs.contains("->")) {
					continue;
				}
				modifyLines.remove(rs);
			}
			log.info("analyze caller modifyLines : {}", modifyLines);
			for (String mItem : modifyLines) {
				String[] mItemArr = mItem.split("rows=");
				if (mItem.length() <= 2) {
					continue;
				}
				// there are two rows
				JSONObject jsonObject = new JSONObject();
				String firstStr = mItemArr[0];
				if (!firstStr.contains("(cost=")) {
					continue;
				}
				if (firstStr.contains("->")) {
					jsonObject.put("stepName",
							firstStr.substring(firstStr.indexOf("->") + 2, firstStr.indexOf("(cost=")).trim());
				} else {
					jsonObject.put("stepName", firstStr.substring(0, firstStr.indexOf("(cost=")).trim());
				}
				String estimateRows = mItemArr[1].substring(0, mItemArr[1].indexOf(" "));
				String actualRows = mItemArr[2].substring(0, mItemArr[2].indexOf(" "));
				if (StringUtils.isNotEmpty(estimateRows) && StringUtils.isNotEmpty(actualRows)
						&& ((Integer.parseInt(estimateRows) - Integer.parseInt(actualRows)) >= diff)) {
					jsonObject.put("estimateRows", mItemArr[1].substring(0, mItemArr[1].indexOf(" ")));
					jsonObject.put("actualRows", mItemArr[2].substring(0, mItemArr[2].indexOf(" ")));
					result.add(jsonObject);
				}
			}
			return result;
		} catch (Exception e) {
			log.error("get Rows diff fail:{}", e.getMessage());
			return result;
		}
	}

	public JSONObject getPartitionData(Task task, ArrayList<String> rsList, String tableName, Connection conn) {
		JSONObject result = new JSONObject();
		JSONArray resultArray = new JSONArray();
		boolean isHasPartKey = false;
		if (task.getSql().contains(" partition")) {
			result.put("partitionResultArray", resultArray);
			result.put("isHasPartKey", false);
			result.put("isByPartition", true);
			return result;
		}
		List<String> columnNameList = getAllColumnNameList(tableName, conn);
		List<String> partitionFilterList = getPartitionFilterList(rsList);
		log.info("get partition data begin, tableName: {}", tableName);
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(PARTITION_LIST_SQL.replace("TABLENAME", tableName));
			while (rs.next()) {
				JSONObject item = JSONObject.parseObject(rs.getString(1));
				// PartitionData
				String partKey =  item.getString("partkey");
				if (StringUtils.isNotEmpty(partKey)) {
					JSONArray partKeyArr =  JSONArray.parseArray(item.getString("partkey"));
					int partKeyPosition = (int) partKeyArr.get(0);
					String partKeyColumnName = columnNameList.get(partKeyPosition - 1);
					if (partitionFilterList.contains(partKeyColumnName)) {
						isHasPartKey = true;
					}
					PartitionDataResp partitionDataResp = new PartitionDataResp();
					partitionDataResp.setPartStrategy(item.getString("partstrategy"));
					partitionDataResp.setPartKey(partKeyColumnName);
					partitionDataResp.setRelPages(item.getString("relpages"));
					partitionDataResp.setRelTuples(item.getString("reltuples"));
					partitionDataResp.setRelallVisible(item.getString("relallvisible"));
					partitionDataResp.setInterval(item.getString("interval"));
					resultArray.add(partitionDataResp);
				}
			}
			result.put("partitionResultArray", JSON.toJSONString(resultArray));
			result.put("isHasPartKey", isHasPartKey);
			log.info("get partition data result : {}", result);
			stmt.close();
			return result;
		} catch (Exception e) {
			log.error("get partition data fail:{}", e.getMessage());
		}
		return result;
	}

	public List<String> getPartitionFilterList(ArrayList<String> rsList) {
		List<String> list = new ArrayList<>();
		try {
			log.info("get partition filter list begin");
			for (String query: rsList) {
				if (!(query.contains("Filter") && !query.contains("Removed"))) {
					continue;
				}
				log.info("get partition filter query, {}", query);
				if (query.contains("AND")) {
					String[] filterArr = query.split("AND");
					for (String filter : filterArr) {
						if (filter.contains("::")) {
							list.add(filter.substring(filter.indexOf("((") + 2, filter.indexOf(")")).trim());
						} else {
							list.add(filter.substring(filter.indexOf("((") + 2, filter.indexOf("=")).trim());
						}
					}
				} else {
					if (query.contains("((")) {
						list.add(query.substring(query.indexOf("((") + 2, query.indexOf(")")).trim());
					} else {
						list.add(query.substring(query.indexOf("(") + 1, query.indexOf("=")).trim());
					}
				}
			}
			log.info("get partition filter list, {}", list);
		} catch (Exception e) {
			log.error("get partition filter list fail : {}" ,e.getMessage());
		}
		return list;
	}

	public List<String> getAllColumnNameList(String tableName, Connection conn) {
		log.info("get all column name begin");
		List<String> columnList = new ArrayList<>();
		if (StringUtils.isEmpty(tableName)) {
			return columnList;
		}
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(QUERY_ALL_COLUMN_NAME.replace("TABLENAME", tableName));
			while (rs.next()) {
				columnList.add(rs.getString(1));
			}
			stmt.close();
		} catch (Exception e) {
			log.error("get all column name fail:{}", e.getMessage());
		}
		return columnList;
	}
}