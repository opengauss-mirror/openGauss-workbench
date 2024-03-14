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
 *  CommonConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/CommonConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

public class CommonConstants {
    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String EQUAL = "=";
    public static final String BLANK = " ";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String INSERT_INTO_PARAM_INFO = "insert into param_info(paramType,paramName,paramDetail,suggestValue,defaultValue,unit,suggestExplain,diagnosisRule) \n";
    public static final String HASH_COND = "Hash Cond";
    public static final String COST = "(cost=";
    public static final String BR = "<br/>";
    public static final String TITLE = "title";
    public static final String SAMPLES = "samples";
    public static final String PARTITION_RESULT_ARRAY = "partitionResultArray";
    public static final String IS_HAS_PART_KEY = "isHasPartKey";

    public static final String GET_INDEX_ADVICE_FAIL = "get index advice fail:{}";
    public static final String TABLENAME = "TABLENAME";
    public static final String DATA_FAIL = "data fail:{}";
    public static final String DIAGNOSIS_RULE = "diagnosisRule";

    /**
     * Max run node num
     */
    public static final int MAX_RUN_NODE_NUM = 5;

    /**
     * Param database name
     */
    public static final String DATABASE_NAME = "data/diagnosisData/";

    /**
     * Check if the port exists
     */
    public static final String PORT_IS_EXIST = "source /etc/profile && lsof -ti :%s && echo 'true' || echo 'false'";

    /**
     * Get pid by port
     */
    public static final String PORT_PID = "ss -tulpn | grep ':%s\\s' | awk '{print $7}' | awk -F \",\" '{print $2}' "
            + "| awk -F \"=\" '{print $2}'";

    /**
     * Check if the directory exists
     */
    public static final String DIRECTORY_IS_EXIST = "[ -e %s ] && echo 'true' || echo 'false'";

    /**
     * Check if the folder is empty
     */
    public static final String DIRECTORY_IS_EMPTY = "find %s -mindepth 1 -print -quit 2>/dev/null";

    /**
     * Check if the file exists
     */
    public static final String FILE_IS_EXIST = "[ -f %s ] && echo 'true' || echo 'false'";

    /**
     * Unzip version
     */
    public static final String UNZIP_VERSION = "unzip -v";

    /**
     * Python version
     */
    public static final String PYTHON_VERSION = "python --version";

    /**
     * Yum install
     */
    public static final String YUM_INSTALL = "yum -y install %s";

    /**
     * Mkdir file
     */
    public static final String MKDIR_FILE = "mkdir -p %s";

    /**
     * Remove file
     */
    public static final String RM_FILE = "rm -rf %s";

    /**
     * Install FlameGraph
     */
    public static final String INSTALL_FLAME_GRAPH = "cd %s && unzip FlameGraph.zip && cd FlameGraph "
            + "&& chmod +x flamegraph.pl";

    /**
     * Java version
     */
    public static final String JDK_VERSION = "source /etc/profile && java -version 2>&1 "
            + "| awk -F '\"' '/version/ {print $2}'";

    /**
     * Tar file
     */
    public static final String TAR_FILE = "tar zxvf %s -C %s";

    /**
     * Mv file
     */
    public static final String MV_FILE = "mv %s %s";
}
