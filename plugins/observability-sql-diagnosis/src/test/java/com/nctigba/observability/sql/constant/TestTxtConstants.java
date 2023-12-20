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
 *  TestTxtConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/constant/TestTxtConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * TestTxtConstants
 *
 * @author luomeng
 * @since 2023/9/21
 */
public class TestTxtConstants {
    /**
     * BIOLATENCY
     */
    public static final String BIOLATENCY = "0 -> 1        : 2        |****************************************|"
            + System.getProperty("line.separator")
            + "2 -> 3     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "4 -> 7     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "8 -> 15     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "16 -> 31     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "32 -> 63     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "64 -> 127     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "128 -> 255     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "256 -> 511     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "512 -> 1023     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "1024 -> 2047     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "2048 -> 4095     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "4096 -> 8191     : 5        |********************                    |";

    /**
     * BIOSNOOP
     */
    public static final String BIOSNOOP =
            "TIME(s)     COMM           PID    DISK    T SECTOR     BYTES  QUE(ms) LAT(ms)"
                    + System.getProperty("line.separator")
                    + "0.000000    ftdc           1364265 vda     W 373572440  20480     0.02    0.21"
                    + System.getProperty("line.separator")
                    + "0.170403    ?              0              R 0          8         0.00    0.53"
                    + System.getProperty("line.separator")
                    + "0.659130    systemd-journa 636    vda     R 5785536    16384     0.01    8.48"
                    + System.getProperty("line.separator")
                    + "0.662596    WALwriter      3825204 vda     W 359195680  8192      0.02    0.20"
                    + System.getProperty("line.separator")
                    + "0.940966    kswapd0        81     vda     W 17452920   4096      0.04    0.20"
                    + System.getProperty("line.separator")
                    + "3.080469    w:gaussdb      3825204 vda     R 353160112  8192      0.02    0.12"
                    + System.getProperty("line.separator")
                    + "1.628842    WALwriter      1371588 vda     W 315020960  16384     0.02    0.21"
                    + System.getProperty("line.separator")
                    + "1.765341    checkpointer   3801724 vda     W 374854128  8192      0.02    0.23"
                    + System.getProperty("line.separator")
                    + "1.820222    checkpointer   3801724 vda     W 416334680  4096      0.02    0.23"
                    + System.getProperty("line.separator")
                    + "1.850895    kworker/4:0    1330903 vda     W 345022008  4096      0.02    0.17"
                    + System.getProperty("line.separator")
                    + "1.891060    kworker/4:0    1330903 vda     W 345022040  4096      0.08    0.16"
                    + System.getProperty("line.separator")
                    + "3.056159    offcputime     1850717 vda     R 283035488  16384     0.03    1.69";

    /**
     * CACHESTAT
     */
    public static final String CACHESTAT = "TIME         HITS   MISSES  DIRTIES HITRATIO   BUFFERS_MB  CACHED_MB"
            + System.getProperty("line.separator")
            + "16:24:45   557619        0     1292  100.00%          722       6464"
            + System.getProperty("line.separator")
            + "16:24:46   353716       26     1255   69.99%          722       6464"
            + System.getProperty("line.separator")
            + "16:24:52   511045      648     2032   99.87%          713       6286"
            + System.getProperty("line.separator")
            + "16:24:57   309824        0     1395  100.00%          713       6288"
            + System.getProperty("line.separator")
            + "16:25:01   326598        0     1300  100.00%          711       6276";

    /**
     * CACHETOP
     */
    public static final String CACHETOP =
            "PID      UID      CMD              HITS     MISSES   DIRTIES  READ_HIT%  WRITE_HIT%"
                    + System.getProperty("line.separator")
                    + "3275659 root     pidstat               531        2      264      50.1%       0.2%"
                    + System.getProperty("line.separator")
                    + "3275659 root     pidstat               531        2      264      50.1%       0.2%"
                    + System.getProperty("line.separator")
                    + "3275659 root     pidstat               531        2      264      50.1%       0.2%"
                    + System.getProperty("line.separator")
                    + "3275659 root     pidstat               531        2      264      50.1%       0.2%"
                    + System.getProperty("line.separator")
                    + "3275659 root     pidstat               531        2      264      50.1%       0.2%";

    /**
     * ColdFunction HotFunction
     */
    public static final String SVG =
            "<title>swapgs_restore_regs_and_return_to_usermode (283 samples, 0.03%)</title><rect x=\"10.6\" "
                    + "y=\"277\" width=\"0.3\" height=\"15.0\" fill=\"rgb(112,112,216)\" rx=\"2\" ry=\"2\" />"
                    + System.getProperty("line.separator")
                    + "<text  x=\"13.60\" y=\"287.5\" ></text>"
                    + System.getProperty("line.separator")
                    + "</g>"
                    + System.getProperty("line.separator")
                    + "<g >"
                    + System.getProperty("line.separator")
                    + "<title>__schedule (89 samples, 0.01%)</title><rect x=\"10.3\" y=\"197\" width=\"0.1\" "
                    + "height=\"15.0\" fill=\"rgb(124,124,223)\" rx=\"2\" ry=\"2\" />"
                    + System.getProperty("line.separator")
                    + "<text  x=\"13.31\" y=\"207.5\" ></text>"
                    + System.getProperty("line.separator")
                    + "</g>"
                    + System.getProperty("line.separator")
                    + "<g >"
                    + System.getProperty("line.separator")
                    + "<title>ExecScan(ScanState*, TupleTableSlot* (*)(ScanState*), bool (*)(ScanState*, "
                    + "TupleTableSlot*)) (1,601 samples, 0.16%)</title><rect x=\"10.6\" y=\"309\" "
                    + "width=\"1.9\" height=\"15.0\" fill=\"rgb(87,87,243)\" rx=\"2\" ry=\"2\" />"
                    + System.getProperty("line.separator")
                    + "<text  x=\"13.60\" y=\"319.5\" ></text>"
                    + System.getProperty("line.separator")
                    + "</g>";

    /**
     * FILETOP
     */
    public static final String FILETOP = "TID    COMM             READS  WRITES R_Kb    W_Kb    T FILE"
            + System.getProperty("line.separator")
            + "1854907 ps               395    0      50547   0       R cmdline"
            + System.getProperty("line.separator")
            + "1854896 ps               395    0      50547   0       R cmdline"
            + System.getProperty("line.separator")
            + "1854900 ps               395    0      50547   0       R cmdline"
            + System.getProperty("line.separator")
            + "1854891 ps               372    0      47603   0       R cmdline"
            + System.getProperty("line.separator")
            + "1854885 ps               206    0      26360   0       R cmdline";

    /**
     * SQL_EXPLAIN
     */
    public static final String SQL_EXPLAIN = "["
            + "  {"
            + "    \"Plan\": {"
            + "      \"Node Type\": \"Sort\","
            + "      \"Startup Cost\": 229633.94,"
            + "      \"Total Cost\": 229633.96,"
            + "      \"Plan Rows\": 6,"
            + "      \"Plan Width\": 12,"
            + "      \"Sort Key\": [\"l_returnflag\", \"l_linestatus\"],"
            + "      \"Plans\": ["
            + "        {"
            + "          \"Node Type\": \"Aggregate\","
            + "          \"Strategy\": \"Hashed\","
            + "          \"Parent Relationship\": \"Outer\","
            + "          \"Startup Cost\": 229633.80,"
            + "          \"Total Cost\": 229633.86,"
            + "          \"Plan Rows\": 6,"
            + "          \"Plan Width\": 12,"
            + "          \"Group By Key\": [\"l_returnflag\", \"l_linestatus\"],"
            + "          \"Plans\": ["
            + "            {"
            + "              \"Node Type\": \"Seq Scan\","
            + "              \"Parent Relationship\": \"Outer\","
            + "              \"Relation Name\": \"lineitem\","
            + "              \"Alias\": \"lineitem\","
            + "              \"Startup Cost\": 0.00,"
            + "              \"Total Cost\": 184624.03,"
            + "              \"Plan Rows\": 6001303,"
            + "              \"Plan Width\": 4,"
            + "              \"Filter\": \"(((s_i_id)::numeric = 5.0) AND (abs(s_w_id) = 5))\""
            + "            }"
            + "          ]"
            + "        }"
            + "      ]"
            + "    }"
            + "  }"
            + "]";

    /**
     * TCPTOP
     */
    public static final String TCPTOP = "PID    COMM         LADDR                 RADDR                  RX_KB  TX_KB"
            + System.getProperty("line.separator")
            + "3692629 filebeat     192.168.0.128:40114   192.168.0.222:9200        30   2368"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52422        6    277"
            + System.getProperty("line.separator")
            + "4040690 opengauss_ex 192.168.0.128:52422   192.168.0.128:5432       277      6"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52498        3    204"
            + System.getProperty("line.separator")
            + "4040690 opengauss_ex 192.168.0.128:52498   192.168.0.128:5432       204      3"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52508        2    201"
            + System.getProperty("line.separator")
            + "4040690 opengauss_ex 192.168.0.128:52508   192.168.0.128:5432       201      2"
            + System.getProperty("line.separator")
            + "4040690 opengauss_ex 192.168.0.128:52412   192.168.0.128:5432       198      2"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52412        2    198"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52478        2    197"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52408        0    196"
            + System.getProperty("line.separator")
            + "970211 gaussdb      192.168.0.128:5432    192.168.0.128:52404        2     68";

    /**
     * XFSDIST
     */
    public static final String XFSDIST = "operation = read" + System.getProperty("line.separator")
            + "usecs               : count     distribution" + System.getProperty("line.separator")
            + "0 -> 1        : 2        |****************************************|"
            + System.getProperty("line.separator")
            + "2 -> 3     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "4 -> 7     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "8 -> 15     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "16 -> 31     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "32 -> 63     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "64 -> 127     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "128 -> 255     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "256 -> 511     : 1        |********************                    |"
            + System.getProperty("line.separator")
            + "512 -> 1023     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "1024 -> 2047     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "2048 -> 4095     : 5        |********************                    |"
            + System.getProperty("line.separator")
            + "4096 -> 8191     : 5        |********************                    |";
}
