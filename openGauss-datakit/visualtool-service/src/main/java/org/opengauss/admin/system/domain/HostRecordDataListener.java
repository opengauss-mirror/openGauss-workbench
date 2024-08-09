package org.opengauss.admin.system.domain;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.HostRecord;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.impl.HostServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel Host Record Listener Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
@Slf4j
public class HostRecordDataListener implements ReadListener<HostRecord> {

    private IHostService hostService;
    private boolean headerChecked = false;
    private String isSwitchingLanguage;

    public HostRecordDataListener() {
        hostService = new HostServiceImpl();
    }

    public HostRecordDataListener(IHostService hostService, String isSwitchingLanguage) {
        this.hostService = hostService;
        this.isSwitchingLanguage = isSwitchingLanguage;
    }

    @Override
    public void invoke(HostRecord data, AnalysisContext context) {
        if (!headerChecked) {
            throw new IllegalArgumentException("Error parsing header.");
        }
        log.info("Parsed a data record: {}", JSON.toJSONString(data));

    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headerChecked = true;
        Set<Map.Entry<Integer, ReadCellData<?>>> entries = headMap.entrySet();
        if (!isHeaderValid(entries)) {
            throw new IllegalArgumentException("Error parsing header.");
        }
    }

    private boolean isHeaderValid(Set<Map.Entry<Integer, ReadCellData<?>>> entries) {
        List<String> expectedzhHeaders = new ArrayList<>();
        expectedzhHeaders.add("序号");
        expectedzhHeaders.add("服务器名称");
        expectedzhHeaders.add("内网IP");
        expectedzhHeaders.add("公网IP");
        expectedzhHeaders.add("端口号");
        expectedzhHeaders.add("用户名称");
        expectedzhHeaders.add("用户密码");
        expectedzhHeaders.add("是否为管理员（是|否）");
        expectedzhHeaders.add("标签");
        expectedzhHeaders.add("备注");

        List<String> expectedenHeaders = new ArrayList<>();
        expectedenHeaders.add("id");
        expectedenHeaders.add("name");
        expectedenHeaders.add("privateIP");
        expectedenHeaders.add("publicIP");
        expectedenHeaders.add("port");
        expectedenHeaders.add("userName");
        expectedenHeaders.add("password");
        expectedenHeaders.add("isAdmin(yes|no)");
        expectedenHeaders.add("tags");
        expectedenHeaders.add("remark");

        boolean containsExpectedHeaders = true;

        for (String expectedHeader : "zh-CN".equals(isSwitchingLanguage) ? expectedzhHeaders : expectedenHeaders) {
            boolean found = false;
            for (Map.Entry<Integer, ReadCellData<?>> entry : entries) {
                if (expectedHeader.equals(entry.getValue().getStringValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                containsExpectedHeaders = false;
                break;
            }
        }
        return containsExpectedHeaders;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        ReadListener.super.onException(exception, context);
        log.error("Parsing failed, but continue to the next line: {}", exception.getMessage());
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            log.error("Parsing exception at line {}, column {}, data: {}", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("All data parsing completed.");
    }
}
