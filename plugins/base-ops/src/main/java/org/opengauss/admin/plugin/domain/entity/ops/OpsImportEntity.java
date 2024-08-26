package org.opengauss.admin.plugin.domain.entity.ops;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opengauss.admin.common.exception.ops.OpsException;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ColumnWidth(25)
public class OpsImportEntity {
    @ExcelProperty(index = 0)
    private String clusterName;
    @ExcelProperty(index = 1)
    private String privateIp;
    @ExcelProperty(index = 2)
    private String publicIp;
    @ExcelProperty(index = 3)
    private String installUsername;
    @ExcelProperty(index = 4)
    private String databaseUsername;
    @ToString.Exclude
    @ExcelProperty(index = 5)
    private String databasePassword;
    @ExcelProperty(index = 6)
    private Integer port;
    @ExcelProperty(index = 7)
    private String envPath;
    @ExcelProperty(index = 8)
    private String importStatus;
    @ExcelProperty(index = 9)
    private String errorInfo;

    public void checkConfig() {
        if (Objects.isNull(clusterName)) {
            throw new OpsException("The clusterName is incorrect");
        }

        if (Objects.isNull(privateIp)) {
            throw new OpsException("Incorrect privateIp");
        }

        if (Objects.isNull(publicIp)) {
            throw new OpsException("Incorrect publicIp");
        }

        if (Objects.isNull(installUsername)) {
            throw new OpsException("Incorrect installUsername");
        }

        if (Objects.isNull(databaseUsername)) {
            throw new OpsException("Incorrect databaseUsername");
        }

        if (Objects.isNull(databasePassword)) {
            throw new OpsException("Incorrect databasePassword");
        }

        if (Objects.isNull(port)) {
            throw new OpsException("Incorrect port");
        }

        if (Objects.isNull(envPath)) {
            throw new OpsException("Incorrect envPath");
        }
    }
}
