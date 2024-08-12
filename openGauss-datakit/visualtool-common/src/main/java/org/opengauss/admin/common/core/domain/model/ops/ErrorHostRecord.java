package org.opengauss.admin.common.core.domain.model.ops;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opengauss.admin.common.utils.excel.TagsConverter;

import java.util.List;

/**
 * Error Report Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ColumnWidth(15)
public class ErrorHostRecord {
    @ExcelProperty("序号")
    private Integer id;
    @ExcelProperty("服务器名称")
    private String name;
    @ExcelProperty("内网IP")
    private String privateIp;
    @ExcelProperty("公网IP")
    private String publicIp;
    @ExcelProperty("端口号")
    private Integer port;
    @ExcelProperty("用户名称")
    private String userName;
    @ExcelProperty("用户密码")
    @ToString.Exclude
    private String password;
    @ExcelProperty(value = "是否为管理员（是|否）")
    private String isAdmin;
    @ExcelProperty(value = "标签", converter = TagsConverter.class)
    private List<String> tags;
    @ColumnWidth(25)
    @ExcelProperty("备注")
    private String remark;
    @ColumnWidth(20)
    @ExcelProperty("时间戳")
    private String timestamp;
}
