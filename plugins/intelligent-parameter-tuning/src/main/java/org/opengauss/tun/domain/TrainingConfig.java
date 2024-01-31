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

package org.opengauss.tun.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * TrainingConfig
 *
 * @author liu liu
 * @since 2023-12-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("training_config")
@Builder
public class TrainingConfig {
    @TableId("training_id")
    private String trainingId;
    private String benchmark;     // 所使用的压测工具 sysbench 或 dwg
    private String clusterName;  // 集群名称
    private String host;        // 数据库ip
    private String port;           // 数据库端口
    private String db;          // 数据库名称
    private String user;        // 数据库用户
    private String password;    // 数据库密码
    private String ommPassword; // omm 用户密码
    private String opengaussNodePath;  // opengauss_node_path
    private String rootPassword;      // 服务器root 用户密码
    private String hasPressureTab;             // 是否有压测表
    private String tables = "20";                         // 表数量
    private String tableSize = "100000";                      // 表数据量大小
    private String mode = "oltp_read_write";    // 压测模式
    private String samplingNumber = "10";                 // 随机采样次数
    private String iteration = "2";                     // 模型推荐次数
    private String threads = "10";                       // 压测线程数
    private String runningTime = "5";                  // 每轮压测时长
    private String rankedKnobsNumber = "10";            // 进行重要性排名后, 选取的配置参数的数量  压测参数数量
    private String online = "False";
    private String finetune = "True";
    private String restartWhenFinetune = "False";       // # 微调过程中是否允许重启
    private String timeWait = "30";                     // 重启数据库的等待时间, 若超时仍未启动, 则恢复数据库
    private String warmUpTime = "10";
    private String schemaName = "public";            // 需要解析的schema名称
    private String remoteCachePath;                 //  执行gs_dump命令后在服务端保存的结果(建表语句等, 用于生成负载)
    private String localCachePath;                  // 执行gs_dump命令后在本地保存的结果
    private String useLocal;                        // 默认不分析本地文件
    private String sqlNum = "1000";                 // 生成负载的规模
    private String readWriteRatio = "0.8";          // 生成负载的读写比 read / tota
    private String jsonExtractResultPath;          //  生成负载的其他配置信息
    private String workload;                      // 生成负载的保存路径, 也是使用dwg压测所用的负载
    private String sqlNumPrint = "500";            // # 每个线程成功执行sql_num_print条sql后在控制台输出信息
    private String memory = "64";
    private String startTime;          // 开始时间
    private String endTime;          // 结束时间
    private String isCustomPayloads = "0";                     // 是否使用自定义负载
    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile[] customLoad = null;                   // 自定义负载sql文件; 追加res res.wg
    private String tableDomainDistribution = "[0.5, 0.5]";             // ##表值域分布 [0.5, 0.5]
    private String queryComparisonOperatorRatio = "[0, 0, 1, 0]";       // ##比较运算符比例  [0, 0, 1, 0],
    private String queryLogicPredicateNum = "[0.6, 0.2, 0.2]";            // ## 逻辑谓词比例 [0.6, 0.2, 0.2],
    private String averageAggregationOperatorNum = "[0.5, 0.3, 0.2]";    // ##平均聚合运算 [0.5, 0.3, 0.2],
    private String averageQueryColumnNum = "[0, 0.6, 0.3, 0.1]";           // ##平均聚合查询列 [0, 0.6, 0.3, 0.1],
    private String groupByRatioIfReadSql = "[0.5, 0.5]";          // ##分组查询占比[0.5, 0.5],
    private String orderByDescOrAscIfGrouped = "[0.5, 0.5]";     // ##排序查询[0.5,0.5]
    private String averageTableNum = "[1.0,0.0]";
    private Integer process;
    private String status;
    private String logPath;     // 训练日志path
    private String fileName;
}

