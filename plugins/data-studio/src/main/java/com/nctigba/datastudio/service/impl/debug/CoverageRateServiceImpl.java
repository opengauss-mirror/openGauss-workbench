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
 *  CoverageRateServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/CoverageRateServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.entity.CoverageRateDO;
import com.nctigba.datastudio.model.query.CoverageRateQuery;
import com.nctigba.datastudio.service.CoverageRateService;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BEGIN;
import static com.nctigba.datastudio.constants.CommonConstants.END;
import static com.nctigba.datastudio.constants.CommonConstants.T_STR;
import static com.nctigba.datastudio.constants.CommonConstants.UNDERLINE;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_BY_ID_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.HIS_COVERAGE_OID_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;

/**
 * CoverageRateServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class CoverageRateServiceImpl implements CoverageRateService {
    public static final String EXPORT_HTML = "<!DOCTYPE html>" + LF
            + "<html>" + LF
            + "<body>" + LF
            + "    <table style=\"height:50px\">" + LF
            + "        <thead>" + LF
            + "            <tr>" + LF
            + "                <th colspan=\"4\" id=\"EXECUTE_STATEMENT\"></th>" + LF
            + "            </tr>" + LF
            + "        </thead>" + LF
            + "        <tbody id=\"executeSql\">" + LF
            + "        </tbody>" + LF
            + "    </table>" + LF
            + "    <table>" + LF
            + "        <thead>" + LF
            + "            <tr>" + LF
            + "                <th id=\"SERIAL_NUMBER\"></th>" + LF
            + "                <th id=\"TOTAL_ROWS\"></th>" + LF
            + "                <th id=\"EXECUTION_ROWS\"></th>" + LF
            + "                <th id=\"TOTAL_COVERAGE\"></th>" + LF
            + "                <th id=\"ALL_LINE_NUMBER\"></th>" + LF
            + "                <th id=\"EXECUTION_LINE_NUMBER\"></th>" + LF
            + "                <th id=\"EXECUTION_COVERAGE\"></th>" + LF
            + "                <th id=\"INPUT_PARAMS\"></th>" + LF
            + "                <th id=\"UPDATE_TIME\"></th>" + LF
            + "            </tr>" + LF
            + "        </thead>" + LF
            + "        <tbody id =\"data\">" + LF
            + "        </tbody>" + LF
            + "    </table>" + LF
            + "</body>" + LF
            + "<style>" + LF
            + "    .bac_nor {" + LF
            + "        border-top: none;" + LF
            + "        border-bottom: none;" + LF
            + "    }" + LF
            + "    .bac_remark {" + LF
            + "        background-color: rgb(167, 169, 169);" + LF
            + "    }" + LF
            + "    .bac_fail {" + LF
            + "        width: 10px;" + LF
            + "        height: 10px;" + LF
            + "        background-color: #ff0000;" + LF
            + "        border-radius: 50%;" + LF
            + "        float: right;" + LF
            + "    }" + LF
            + "    .bac_pass {" + LF
            + "        width: 10px;" + LF
            + "        height: 10px;" + LF
            + "        background-color: #008000;" + LF
            + "        border-radius: 50%;" + LF
            + "        float: right;" + LF
            + "    }" + LF
            + "" + LF
            + "    table {" + LF
            + "        width: 100%;" + LF
            + "        height: 200px;" + LF
            + "        border-collapse: collapse;" + LF
            + "        border: 1px solid black;" + LF
            + "    }" + LF
            + "" + LF
            + "    td {" + LF
            + "        border: 1px solid black;" + LF
            + "        text-align: center;" + LF
            + "    }" + LF
            + "" + LF
            + "    thead {" + LF
            + "        background-color: rgb(220, 157, 157);" + LF
            + "    }" + LF
            + "</style>" + LF
            + "</html>";
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public List<CoverageRateDO> queryCoverageRate(CoverageRateQuery request) throws SQLException {
        log.info("CoverageRateServiceImpl queryCoverageRate request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(HIS_COVERAGE_OID_SQL, request.getOid()))
        ) {
            List<CoverageRateDO> list = getCoverageList(resultSet);
            log.info("CoverageRateServiceImpl queryCoverageRate list: " + list);
            return list;
        }
    }

    @Override
    public void delete(CoverageRateQuery request) throws SQLException {
        log.info("CoverageRateServiceImpl delete request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            List<Long> cidList = request.getCidList();
            String str = DebugUtils.listToString(cidList, COMMA);
            statement.execute(String.format(DELETE_BY_ID_SQL, str));
            log.info("CoverageRateServiceImpl delete end ");
        }
    }

    @Override
    public void export(CoverageRateQuery request, HttpServletResponse response)
            throws SQLException, IOException, NoSuchFieldException, IllegalAccessException {
        log.info("CoverageRateServiceImpl export request: " + request);
        List<CoverageRateDO> list;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(HIS_COVERAGE_OID_SQL, request.getOid()))
        ) {
            list = getCoverageList(resultSet);
            log.info("CoverageRateServiceImpl export list: " + list);
        }

        String html = parseHtml(list, request.getCidList());
        log.info("CoverageRateServiceImpl export html: " + html);

        String fileName = "函数覆盖率报告_" + request.getOid() + UNDERLINE + list.get(0).getName()
                + UNDERLINE + System.currentTimeMillis() + ".html";
        log.info("CoverageRateServiceImpl export fileName: " + fileName);

        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.addHeader("Response-Type", "blob");
        response.setCharacterEncoding("UTF-8");
        try (
                InputStream inputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
                OutputStream outputStream = response.getOutputStream()
        ) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        }
    }

    private List<CoverageRateDO> getCoverageList(ResultSet resultSet) throws SQLException {
        List<CoverageRateDO> list = new ArrayList<>();
        int index = 1;
        while (resultSet.next()) {
            CoverageRateDO coverageRateDO = new CoverageRateDO();
            coverageRateDO.setSerialNumber(index++);
            String proName = resultSet.getString("pro_name");
            coverageRateDO.setName(proName);
            Long proOid = resultSet.getLong("pro_oid");
            coverageRateDO.setOid(proOid);
            Long coverageId = resultSet.getLong("coverage_id");
            coverageRateDO.setCid(coverageId);

            int totalLine = 0;
            StringBuilder canBreakLine = new StringBuilder();
            String proCanBreak = resultSet.getString("pro_canbreak");
            String[] canBreakSplit = proCanBreak.substring(1, proCanBreak.length() - 1).split(COMMA);
            for (int i = 0; i < canBreakSplit.length; i++) {
                if (T_STR.equals(canBreakSplit[i])) {
                    totalLine++;
                    canBreakLine.append(i + 1).append(COMMA);
                }
            }
            canBreakLine.deleteCharAt(canBreakLine.length() - 1);

            int executeLine = 0;
            StringBuilder coverageLines = new StringBuilder();
            String coverage = resultSet.getString("coverage");
            String[] coverageSplit = coverage.substring(1, proCanBreak.length() - 1).split(COMMA);
            for (int i = 0; i < coverageSplit.length; i++) {
                if ("1".equals(coverageSplit[i])) {
                    executeLine++;
                    coverageLines.append(i + 1).append(COMMA);
                }
            }
            coverageLines.deleteCharAt(coverageLines.length() - 1);

            coverageRateDO.setTotalRows(totalLine);
            coverageRateDO.setExecutionRows(executeLine);
            BigDecimal divide = BigDecimal.valueOf(executeLine * 100L).divide(BigDecimal.valueOf(totalLine), 1,
                    RoundingMode.CEILING);
            coverageRateDO.setTotalCoverage(divide + "%");
            coverageRateDO.setAllLineNumber(canBreakLine.toString());
            coverageRateDO.setExecutionLineNumber(coverageLines.toString());
            coverageRateDO.setExecutionCoverage(divide + "%");
            coverageRateDO.setInputParams("params");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            coverageRateDO.setUpdateTime(df.format(new Date()));
            String proQuery = resultSet.getString("pro_querys");
            coverageRateDO.setSourcecode(proQuery);
            log.info("CoverageRateServiceImpl getCoverageList coverageRateDO: " + coverageRateDO);
            list.add(coverageRateDO);
        }
        return list;
    }

    private String parseHtml(List<CoverageRateDO> coverageRateList, List<Long> cidList)
            throws NoSuchFieldException, IllegalAccessException, IOException {
        Document document = Jsoup.parse(EXPORT_HTML, "gbk");
        convert(document);
        log.info("CoverageRateServiceImpl parseHtml document: " + document);

        Element data = document.getElementById("data");
        for (CoverageRateDO coverageRateDO : coverageRateList) {
            if (!cidList.contains(coverageRateDO.getCid())) {
                continue;
            }
            log.info("CoverageRateServiceImpl parseHtml coverageRateDO: " + coverageRateDO);
            List<String> list = beanToList(coverageRateDO);
            Element dataTr = data.appendElement("tr");
            list.forEach(item -> {
                Element dataTd = dataTr.appendElement("td");
                dataTd.text(item);
            });

            boolean[] flag = new boolean[]{true};
            Element executeSql = document.getElementById("executeSql");
            Map<String, Integer> offset = new HashMap<>();
            Map<Integer, String> sqlMap = splitSourceCode(coverageRateDO.getSourcecode(), offset);
            sqlMap.forEach((k, v) -> {
                Element tr = executeSql.appendElement("tr");
                if (flag[0]) {
                    Element td = tr.appendElement("td");
                    td.attr("rowspan", sqlMap.values().size() + "");
                    td.text(String.valueOf(coverageRateDO.getSerialNumber()));
                    flag[0] = false;
                }

                Element serial = tr.appendElement("td");
                serial.text(k.toString());
                Element td = tr.appendElement("td");
                Element td2 = tr.appendElement("td");
                Element div = td.appendElement("div");
                td2.text(v);

                if (Arrays.asList(coverageRateDO.getAllLineNumber().split(COMMA)).contains(k.toString())) {
                    td2.addClass("bac_remark");
                }
                boolean isMarkRow = Arrays.asList(coverageRateDO.getAllLineNumber().split(COMMA)).contains(
                        String.valueOf(k));
                if (Arrays.asList(coverageRateDO.getExecutionLineNumber().split(COMMA)).contains(
                        (k - 1) + "") && isMarkRow) {
                    div.addClass("bac_pass");
                } else {
                    if (k > (offset.get(BEGIN) + 1) && k < (offset.get(END) + 1) && isMarkRow) {
                        div.addClass("bac_fail");
                    }
                }
            });
        }
        return document.outerHtml();
    }

    private void convert(Document document) {
        Element execStatement = document.getElementById("EXECUTE_STATEMENT");
        execStatement.text(LocaleStringUtils.transLanguage("3001"));
        Element serial = document.getElementById("SERIAL_NUMBER");
        serial.text(LocaleStringUtils.transLanguage("3002"));
        Element totalRows = document.getElementById("TOTAL_ROWS");
        totalRows.text(LocaleStringUtils.transLanguage("3003"));
        Element totalExecLines = document.getElementById("EXECUTION_ROWS");
        totalExecLines.text(LocaleStringUtils.transLanguage("3004"));
        Element totalCoverage = document.getElementById("TOTAL_COVERAGE");
        totalCoverage.text(LocaleStringUtils.transLanguage("3005"));
        Element markRows = document.getElementById("ALL_LINE_NUMBER");
        markRows.text(LocaleStringUtils.transLanguage("3006"));
        Element markExecLines = document.getElementById("EXECUTION_LINE_NUMBER");
        markExecLines.text(LocaleStringUtils.transLanguage("3007"));
        Element markCoverage = document.getElementById("EXECUTION_COVERAGE");
        markCoverage.text(LocaleStringUtils.transLanguage("3008"));
        Element inputParams = document.getElementById("INPUT_PARAMS");
        inputParams.text(LocaleStringUtils.transLanguage("3009"));
        Element updateTime = document.getElementById("UPDATE_TIME");
        updateTime.text(LocaleStringUtils.transLanguage("3010"));
    }

    private List<String> beanToList(CoverageRateDO coverageRateDO) throws NoSuchFieldException, IllegalAccessException {
        log.info("CoverageRateServiceImpl beanToList coverageRateDO: " + coverageRateDO);
        List<String> list = new ArrayList<>();
        Field[] fields = coverageRateDO.getClass().getDeclaredFields();
        for (int i = 3; i < fields.length - 1; i++) {
            Field field = coverageRateDO.getClass().getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            Object obj = field.get(coverageRateDO);
            list.add(obj.toString());
        }

        log.info("CoverageRateServiceImpl beanToList list: " + list);
        return list;
    }

    private Map<Integer, String> splitSourceCode(String sourceCode, Map<String, Integer> offsetMap) throws IOException {
        log.info("CoverageRateServiceImpl splitSourceCode sourceCode: " + sourceCode);
        Map<Integer, String> map = new HashMap<>();
        try (
                InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(sourceCode.getBytes()));
                BufferedReader br = new BufferedReader(isr)
        ) {
            String line;
            int index = 1;
            while ((line = br.readLine()) != null) {
                map.put(index, line);
                if (line.trim().equalsIgnoreCase(BEGIN)) {
                    offsetMap.put(BEGIN, index);
                }
                if (line.trim().toLowerCase().startsWith(END)) {
                    offsetMap.put(END, index);
                }
                index++;
            }
        }
        log.info("CoverageRateServiceImpl splitSourceCode map: " + map);
        log.info("CoverageRateServiceImpl splitSourceCode offsetMap: " + offsetMap);
        return map;
    }
}
