/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.admin.common.utils.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.opengauss.admin.common.core.dto.ops.OpsJdbcClusterImportDto;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ops jdbc db cluster import listener
 *
 * @since 2026/7/15
 */
@Slf4j
public class OpsJdbcClusterImportListener extends AnalysisEventListener<OpsJdbcClusterImportDto> {
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        Map<Integer, String> expectedHeadMap = getExpectedHeaderMap();
        for (Map.Entry<Integer, String> entry : expectedHeadMap.entrySet()) {
            int index = entry.getKey();
            String expectedHeader = entry.getValue();
            String actualHeader = headMap.get(index);

            if (!expectedHeader.equals(actualHeader)) {
                throw new IllegalArgumentException("Header error, column: " + (index + 1)
                        + ", expected: " + expectedHeader + ", actual: " + actualHeader);
            }
        }
    }

    @Override
    public void invoke(OpsJdbcClusterImportDto opsJdbcClusterImportDto, AnalysisContext analysisContext) {
        String errorMsg = validateFields(opsJdbcClusterImportDto, analysisContext.readRowHolder().getRowIndex() + 1);
        if (errorMsg.isEmpty()) {
            convertFields(opsJdbcClusterImportDto);
        } else {
            opsJdbcClusterImportDto.setErrorMsg(errorMsg);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("Jdbc cluster import excel file processed");
    }

    private Map<Integer, String> getExpectedHeaderMap() {
        Map<Integer, String> map = new HashMap<>();
        Field[] fields = OpsJdbcClusterImportDto.class.getDeclaredFields();
        for (Field field : fields) {
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                int index = annotation.index();
                String headerName = annotation.value().length > 0 ? annotation.value()[0] : "";
                map.put(index, headerName);
            }
        }
        return map;
    }

    private String validateFields(OpsJdbcClusterImportDto opsJdbcClusterImportDto, Integer rowNum) {
        StringBuilder errorMsgBuilder = new StringBuilder();
        if (ObjectUtils.isEmpty(opsJdbcClusterImportDto.getClusterName())) {
            errorMsgBuilder.append(" Cluster Name cannot be empty.");
        }

        String databaseType = opsJdbcClusterImportDto.getDatabaseType();
        if (ObjectUtils.isEmpty(databaseType)) {
            errorMsgBuilder.append(" Database Type cannot be empty.");
        } else {
            databaseType = databaseType.toUpperCase(Locale.ROOT);
            if (!DbTypeEnum.OPENGAUSS.name().equals(databaseType)
                    && !DbTypeEnum.MYSQL.name().equals(databaseType)
                    && !DbTypeEnum.POSTGRESQL.name().equals(databaseType)) {
                errorMsgBuilder.append(" Invalid Database Type, supported: openGauss, MySQL, PostgreSQL.");
            }
        }

        if (ObjectUtils.isEmpty(opsJdbcClusterImportDto.getIp())) {
            errorMsgBuilder.append(" Node IP cannot be empty.");
        }

        if (ObjectUtils.isEmpty(opsJdbcClusterImportDto.getPort())) {
            errorMsgBuilder.append(" Port cannot be empty.");
        } else {
            try {
                int portNum = Integer.parseInt(opsJdbcClusterImportDto.getPort());
                if (portNum < 1 || portNum > 65535) {
                    errorMsgBuilder.append(" Port must be between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                errorMsgBuilder.append(" Port must be a number.");
            }
        }

        if (ObjectUtils.isEmpty(opsJdbcClusterImportDto.getUsername())) {
            errorMsgBuilder.append(" Username cannot be empty.");
        }
        if (ObjectUtils.isEmpty(opsJdbcClusterImportDto.getPassword())) {
            errorMsgBuilder.append(" Password cannot be empty.");
        }

        if (!errorMsgBuilder.isEmpty()) {
            return String.format(Locale.ROOT, "Row %d:%s", rowNum, errorMsgBuilder.toString());
        }
        return "";
    }

    private void convertFields(OpsJdbcClusterImportDto opsJdbcClusterImportDto) {
        opsJdbcClusterImportDto.setPortInt(Integer.parseInt(opsJdbcClusterImportDto.getPort()));
        opsJdbcClusterImportDto.setDbType(DbTypeEnum.typeOf(opsJdbcClusterImportDto.getDatabaseType()));
    }
}

