package org.opengauss.admin.common.utils.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.List;

/**
 * Excel to Java Object Conversion Administrator Converter Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
public class IsAdminConverter implements Converter<Integer> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String value = cellData.getStringValue();
        if ("是".equals(value)) return 0;
        else return 1;
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) throws Exception {
        Integer value = context.getValue();
        if (0 == value) return new WriteCellData<>("是");
        else return new WriteCellData<>("否");
    }
}
