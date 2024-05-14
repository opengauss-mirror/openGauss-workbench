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

package org.opengauss.tun.common;

import java.util.Map;

/**
 * FixedTuning
 *
 * @author liu
 * @since 2023-12-08
 */
public class FixedTuning {
    /**
     * JOB_DEFAULT
     */
    public static final String JOB_DEFAULT = "default";

    /**
     * SUCCESS_INSTALL
     */
    public static final String SUCCESS_INSTALL = "execution succeeded";

    /**
     * CHECK_PYTHON_VERSION
     */
    public static final String CHECK_PYTHON_VERSION = "python3 -c 'import sys; print(sys.version_info >= (3, 7, 0))'";

    /**
     * BOOL_TRUE
     */
    public static final String BOOL_TRUE = "True";

    /**
     * TIME_OUT
     */
    public static final Long TIME_OUT = 10L;

    /**
     * TIME_OUT_MAIN
     */
    public static final Long TIME_OUT_MAIN = 30L;

    /**
     * DETERMINE
     */
    public static final String DETERMINE = "&& if [ $? -eq 0 ]; then echo execution succeeded; else echo failure; fi";

    /**
     * TUN_DTO
     */
    public static final String TUN_DTO = "tunDto";

    /**
     * TRAININGID
     */
    public static final String TRAININGID = "trainingId";

    /**
     * COMMAND_EXECUTE_LOG_PATH
     */
    public static final String COMMAND_EXECUTE_LOG_PATH = "data/tuning/log/%s_command.log";

    /**
     * INITIALIZATION
     */
    public static final String INITIALIZATION = "初始参数";

    /**
     * SYSBENCH
     */
    public static final String SYSBENCH = "sysbench";

    /**
     * DWG
     */
    public static final String DWG = "dwg";

    /**
     * RUN_FAILED
     */
    public static final String RUN_FAILED = "2";

    /**
     * RUN_SUCCESS
     */
    public static final String RUN_SUCCESS = "1";

    /**
     * RUNING
     */
    public static final String RUNING = "0";

    /**
     * UNZIP
     */
    public static final Integer ENV = 1;

    /**
     * UNZIP
     */
    public static final Integer UNZIP = 5;

    /**
     * PYTHON_ENV_PROC
     */
    public static final Integer PYTHON_ENV_PROC = 10;

    /**
     * PYTHON_ENV_PROC
     */
    public static final Integer PYTHON_HEBO_PROC = 15;

    /**
     * PYTHON_STRATEGY_PROC
     */
    public static final Integer PYTHON_STRATEGY_PROC = 20;

    /**
     * initValue
     */
    public static final Integer INIT_VALUE = 0;

    /**
     * SEARCH_DATABASE
     */
    public static final String SEARCH_DATABASE = "SELECT datname FROM pg_database";

    /**
     * SEARCH_RES
     */
    public static final String SEARCH_RES = "datname";

    /**
     * EXECUTE_JOB
     */
    public static final String EXECUTE_JOB = "_tuning";

    /**
     * EXECUTE_JOB
     */
    public static final String PROCESS_JOB = "_process";

    /**
     * TPYE_MAP
     */
    public static final Map<String, String> TPYE_MAP = Map.of(
            "sysbench", "SYSBENCH",
            "dwg", "DWG"
    );

    /**
     * WORK_PATH
     */
    public static final String WORK_PATH = "data/tuning/";

    /**
     * uploadFilePath
     */
    public static final String UPLOAD_FILE_PATH = "data/tuning/files/";

    /**
     * DB_LOG
     */
    public static final String DB_LOG = "log/db_log/";

    /**
     * TUNE_LOG
     */
    public static final String TUNE_LOG = "log/tune_log/";

    /**
     * BENCHMARK_LOG
     */
    public static final String BENCHMARK_LOG = "log/benchmark_log/";

    /**
     * EXECUTE_FILE_PATH
     */
    public static final String EXECUTE_FILE_PATH = "data/tuning/%s_work/";

    /**
     * SQL_PATH
     */
    public static final String SQL_PATH = "db/main.sql";

    /**
     * URL
     */
    public static final String URL = "jdbc:sqlite:data/tuning.db";

    /**
     * DRIVER_CLASS_NAME
     */
    public static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC";

    /**
     * SYSBENCH
     */
    public static final String SYSBENCH_FILE_PATH = "data/";

    /**
     * MAIN_PATH
     */
    public static final String MAIN_PATH = "opengauss_tuning/";

    /**
     * SYSBENCH_EXECUTE_PATH
     */
    public static final String SYSBENCH_EXECUTE_PATH = "data";

    /**
     * ENV_YUM
     */
    public static final String ENV_YUM = "yum -y install postgresql-devel"
            + "&& yum install gcc libffi-devel python3-devel openssl-devel -y" + DETERMINE;

    /**
     * CREATA_SYSBENCH_ENV
     */
    public static final String CREATA_SYSBENCH_ENV = "yum -y install make automake libtool pkgconfig libaio-devel "
            + "&& yum -y install mariadb-devel openssl-devel "
            + "&& ./autogen.sh "
            + "&& ./configure --with-pgsql "
            + "&& make -j && make install " + DETERMINE;

    /**
     * PYTHON3_ENV
     */
    public static final String PYTHON3_ENV = "pip3 install -r requirements.txt " + DETERMINE;

    /**
     * CHECK_SYSBENCH_VERSION
     */
    public static final String CHECK_SYSBENCH_VERSION = "sysbench --version " + DETERMINE;

    /**
     * JSON_HELPER_COMMAND
     */
    public static final String JSON_HELPER_COMMAND = "python3 SuperWG/DWG/jsonHelper/jsonHelper.py " + DETERMINE;

    /**
     * WORKLOAD_PATH
     */
    public static final String WORKLOAD_COMMAND = "python3 SuperWG/DWG/src/WorkloadGenerator.py " + DETERMINE;

    /**
     * LOAD_FILE_PATH
     */
    public static final String LOAD_FILE_PATH = "workloads/res.wg";

    /**
     * MAIN_COMMAND
     */
    public static final String MAIN_COMMAND = "python3 main.py " + DETERMINE;

    /**
     * HEBO_COMMAND
     */
    public static final String HEBO_COMMAND = "pip3 install -e . " + DETERMINE;

    /**
     * EXECUTE_FILE_PATH
     */
    public static final String HEBO_ENV = "HEBO";

    /**
     * PYTHON_CONF
     */
    public static final String PYTHON_CONF = "opengauss_tuning/config.py";

    /**
     * JSONHELP_CONF
     */
    public static final String JSONHELP_CONF = "opengauss_tuning/SuperWG/DWG/jsonHelper/config.py";

    /**
     * WORKLOAD_CONF
     */
    public static final String WORKLOAD_CONF = "opengauss_tuning/SuperWG/DWG/src/config.py";

    /**
     * TUNING_PATH
     */
    public static final String TUNING_PATH = "tun/opengauss_tuning.zip";

    /**
     * SYSBENCH_PATH
     */
    public static final String SYSBENCH_PATH = "sysbench/sysbench-1.0.17.zip";

    /**
     * TUNING_PATH
     */
    public static final String TUNING_DB = "data/tuning.db";

    /**
     * APPLY_TO_DATABASE_COMMAND_OFFLINE
     */
    public static final String APPLY_TO_DATABASE_COMMAND_OFFLINE = "gs_guc set -c %s -D %s";

    /**
     * APPLY_TO_DATABASE_COMMAND_ONLINE
     */
    public static final String APPLY_TO_DATABASE_COMMAND_ONLINE = "gs_guc reload -c %s -D %s";

    /**
     * DATABASE_COMMAND_RESTART
     */
    public static final String DATABASE_COMMAND_RESTART = "gs_ctl restart -D %s -M primary";

    /**
     * FALSE
     */
    public static final String FALSE = "False";

    /**
     * TRUE
     */
    public static final String TRUE = "True";

    /**
     * STOP
     */
    public static final String STOP = "pkill -f \"python3 SuperWG/DWG/src/WorkloadGenerator.py\" "
            + "&& pkill -f \"python3 SuperWG/DWG/jsonHelper/jsonHelper.py\" && pkill -f \"python3 main.py\"";
}
