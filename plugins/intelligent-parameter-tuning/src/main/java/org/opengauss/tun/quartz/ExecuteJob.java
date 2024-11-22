/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.quartz;

import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.io.PrintWriter;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.tun.cache.CacheFactory;
import org.opengauss.tun.cache.TunCache;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.domain.builder.TuningBuilder;
import org.opengauss.tun.domain.dto.TuningDto;
import org.opengauss.tun.enums.help.TuningHelper;
import org.opengauss.tun.enums.impl.TuningEnum;
import org.opengauss.tun.utils.CommandLineRunner;
import org.opengauss.tun.utils.file.FileExtractor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * ExecuteJob
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
public class ExecuteJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        TuningDto dto = new TuningDto();
        Object obj = dataMap.get(FixedTuning.TUN_DTO);
        if (obj != null && obj instanceof TuningDto) {
            dto = (TuningDto) obj;
        }
        String executePath = dto.getExecutePath();
        String workPath = dto.getWorkPath();
        TrainingConfig config = dto.getConfig();
        String recordPath = config.getLogPath();
        TuningHelper.record("Start executing training tasks", recordPath);
        TuningHelper.record("Put task status running into cache", recordPath);
        OpsJdbcDbClusterNodeEntity node = dto.getNode();
        // Unzip Python script
        unzipPythonScript(executePath, config.getLogPath());
        // Creating a Python environment
        createPythonEnvironment(config, workPath, recordPath);
        // Create Hebo environment
        createHeboEnvironment(config, workPath, recordPath);
        // python更新进度1
        TuningHelper.updateProcess(config.getTrainingId(), FixedTuning.ENV);
        // write file
        writeConfigPy(config, executePath + FixedTuning.PYTHON_CONF);
        writeConfigPy(config, executePath + FixedTuning.JSONHELP_CONF);
        writeConfigPy(config, executePath + FixedTuning.WORKLOAD_CONF);
        // 写完文件更新进度5
        TuningHelper.updateProcess(config.getTrainingId(), FixedTuning.UNZIP);
        TuningEnum.valueOf(FixedTuning.TPYE_MAP.get(config.getBenchmark()))
                .startIntelligentTuning(config, workPath, executePath, node, dto.getSysbenchCommand());
    }

    private void unzipPythonScript(String executePath, String recordPath) {
        TuningHelper.record("Start creating Python file", recordPath);
        FileExtractor.extractResource(FixedTuning.TUNING_PATH, executePath);
    }

    private void createPythonEnvironment(TrainingConfig config, String workPath, String recordPath) {
        TuningHelper.record("Start creating Python execution environment", recordPath);
        TunCache<String, String> cache = CacheFactory.getPythonCache();
        if (cache.includeKey(config.getClusterName())) {
            return;
        }
        String errMsg = "Failed to create python environment, updating task status to failed";
        boolean isSuccess = CommandLineRunner.runCommand(FixedTuning.PYTHON3_ENV, workPath, config.getLogPath(),
                FixedTuning.TIME_OUT);
        String requirementsSuccMsg = "Successfully executed the installation commands:"
                + "- pip3 install -r requirements.txt";
        handleRes(isSuccess, requirementsSuccMsg, errMsg, recordPath, config);
    }

    private void createHeboEnvironment(TrainingConfig config, String workPath, String recordPath) {
        TunCache<String, String> cache = CacheFactory.getPythonCache();
        String clusterName = config.getClusterName();
        String heboEnvPath = workPath + FixedTuning.HEBO_ENV;
        if (!cache.includeKey(clusterName)) {
            TuningHelper.record("Start executing pip3 install -e .", recordPath);
            Boolean isHebo = CommandLineRunner.runCommand(FixedTuning.HEBO_COMMAND, heboEnvPath, config.getLogPath(),
                    FixedTuning.TIME_OUT);
            if (isHebo) {
                cache.put(clusterName, clusterName);
            }
            String succMsg = "Successfully executed pip3 install -e.";
            String errMsg = "Execute pip3 install -e. Failed, updating task status to failed";
            handleRes(isHebo, succMsg, errMsg, recordPath, config);
        }
    }

    private void writeConfigPy(TrainingConfig train, String filePath) {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            TuningBuilder builder = new TuningBuilder();
            builder.appendNote("# 集群id").appendProperty("tuning_id", String.valueOf(train.getTrainingId()))
                    .appendNote("# 调优配置").appendNote("# 随机采样次数")
                    .appendProperty("sampling_number", String.valueOf(train.getSamplingNumber()))
                    .appendNote("# 模型推荐次数").appendProperty("iteration", String.valueOf(train.getIteration()))
                    .appendNote("# 调优方式, 在线True, 离线False")
                    .appendProperty("online", train.getOnline()).appendNote("# 是否进行微调, iteration为微调次数")
                    .appendProperty("finetune", train.getFinetune()).appendNote("# 微调过程中是否允许重启")
                    .appendProperty("restart_when_finetune", train.getRestartWhenFinetune())
                    .appendNote("# 数据库待调整的配置参数文件").appendProperty("knobs_file", "'knobs_config/knobs-43.json'")
                    .appendNote("# 进行重要性排名后, 选取的配置参数的数量")
                    .appendProperty("ranked_knobs_number", train.getRankedKnobsNumber())
                    .appendProperty("method", "'HEBO'").appendProperty("log_path", "'log'")
                    .appendProperty("final_results", "'final_results'")
                    .appendProperty("knowledge", "'knowledge.json'").appendSectionBreak().appendSectionBreak()
                    .appendNote("# 数据库配置").appendProperty("host", "'" + train.getHost() + "'")
                    .appendProperty("port", train.getPort()).appendProperty("db", "'" + train.getDb() + "'")
                    .appendProperty("user", "'" + train.getUser() + "'")
                    .appendProperty("password", "'" + train.getPassword() + "'")
                    .appendProperty("os_user", "'" + train.getOsUser() + "'")
                    .appendProperty("omm_password", "'" + train.getOmmPassword() + "'")
                    .appendProperty("opengauss_node_path", "'" + train.getOpengaussNodePath() + "'")
                    .appendProperty("root_password", "'" + train.getRootPassword() + "'")
                    .appendNote("# 重启数据库的等待时间, 若超时仍未启动, 则恢复数据库").appendProperty("time_wait", "30")
                    .appendSectionBreak().appendSectionBreak().appendNote("# 所使用的压测工具 sysbench 或 dwg")
                    .appendProperty("benchmark", "'" + train.getBenchmark() + "'")
                    .appendNote("# 压测线程数").appendProperty("threads", train.getThreads())
                    .appendSectionBreak().appendSectionBreak().appendNote("# sysbench配置")
                    .appendProperty("tables", train.getTables()).appendProperty("table_size", train.getTableSize())
                    .appendProperty("mode", "'" + train.getMode() + "'")
                    .appendProperty("runing_time", train.getRunningTime())
                    .appendNote("# 正式压测之前预热时间, 结果不计算在最终tps中")
                    .appendProperty("warm_up_time", train.getWarmUpTime())
                    .appendSectionBreak().appendSectionBreak().appendNote("# dwg配置").appendNote("# 需要解析的schema名称")
                    .appendProperty("schema_name", "'" + train.getSchemaName() + "'")
                    .appendNote("# 执行gs_dump命令后在服务端保存的结果(建表语句等, 用于生成负载)")
                    .appendProperty("remote_cache_path", "'/home/" + train.getOsUser() + "/test.sql'");
            writeConfigDwg(builder, train);
            writer.write(builder.build());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private TuningBuilder writeConfigDwg(TuningBuilder builder, TrainingConfig train) {
        return builder.appendNote("# 执行gs_dump命令后在本地保存的结果")
                .appendProperty("local_cache_path", "'workloads/schema/test.sql'")
                .appendNote("# 默认不分析本地文件").appendProperty("use_local", "'false'")
                .appendNote("# 生成负载的规模").appendProperty("sql_num", train.getSqlNum())
                .appendNote("# 生成负载的读写比 read / total")
                .appendProperty("read_write_ratio", train.getReadWriteRatio())
                .appendNote("# 生成负载的其他配置信息")
                .appendProperty("json_extract_result_path", "'" + "workloads/schema/res.json" + "'")
                .appendNote("# 生成负载的保存路径, 也是使用dwg压测所用的负载")
                .appendProperty("workload", "'" + "workloads/res.wg" + "'")
                .appendProperty("workload_restore", "'workloads/restore.sql'")
                .appendNote("# 每个线程成功执行sql_num_print条sql后在控制台输出信息")
                .appendProperty("sql_num_print", train.getSqlNumPrint())
                .appendSectionBreak().appendSectionBreak().appendNote("# hardware configs")
                .appendProperty("memory", train.getMemory()).appendSectionBreak()
                .appendProperty("averageTableNum", train.getAverageTableNum())
                .appendProperty("table_domain_distribution", train.getTableDomainDistribution())
                .appendProperty("query_comparison_operator_ratio", train.getQueryComparisonOperatorRatio())
                .appendProperty("query_logic_predicate_num", train.getQueryLogicPredicateNum())
                .appendProperty("average_aggregation_operator_num", train.getAverageAggregationOperatorNum())
                .appendProperty("average_query_column_num", train.getAverageQueryColumnNum())
                .appendProperty("group_by_ratio_if_read_sql", train.getGroupByRatioIfReadSql())
                .appendProperty("order_by_desc_or_asc_if_grouped", train.getOrderByDescOrAscIfGrouped())
                .appendProperty("taskset", "False").appendProperty("distributed", train.getDistributed())
                .appendProperty("chroot", train.getChroot()).appendProperty("dist_dn_port", train.getDistDnPort())
                .appendProperty("restart_when_online", "False").appendProperty("init_param", train.getInitParam())
                .appendProperty("index_weight", "1")
                .appendProperty("primary_key_weight", "1").appendProperty("backup_path",
                        "'/data/bak_gaussdb_data/sys/data'");
    }

    private void handleRes(Boolean isSuccess, String succMsg, String errorMsg, String recordPath,
                           TrainingConfig config) {
        String message = isSuccess ? succMsg : errorMsg;
        if (!isSuccess) {
            TuningHelper.updateStatusFailed(config.getTrainingId());
        }
        TuningHelper.record(message + StrUtil.LF, recordPath);
    }
}
