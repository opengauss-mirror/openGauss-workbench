package org.opengauss.admin.plugin.domain.entity.ops;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opengauss.admin.common.exception.ops.OpsException;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpsImportEntity {
    @ExcelProperty("集群名")
    private String clusterName;
    @ExcelProperty("内网IP")
    private String privateIp;
    @ExcelProperty("外网IP")
    private String publicIp;
    @ExcelProperty("安装用户")
    private String installUsername;
    @ExcelProperty("数据库用户名")
    private String databaseUsername;
    @ToString.Exclude
    @ExcelProperty("数据库密码")
    private String databasePassword;
    @ExcelProperty("端口号")
    private Integer port;
    @ExcelProperty("环境分离文件路径")
    private String envPath;
    @ExcelProperty("执行状态")
    private String importStatus;
    @ExcelProperty("报错信息")
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
