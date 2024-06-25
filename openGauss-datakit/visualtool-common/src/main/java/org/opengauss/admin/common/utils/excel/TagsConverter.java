package org.opengauss.admin.common.utils.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Excel to Java Object Conversion Label Converter Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
public class TagsConverter implements Converter<List<String>> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public List<String> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        List<String> resultList = Arrays.asList(cellData.getStringValue().split(","));
        return resultList;
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<List<String>> context) throws Exception {
        List<String> value = context.getValue();
        String str = String.join(",", value);
        return new WriteCellData<>(str);
    }
}
