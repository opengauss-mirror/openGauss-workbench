package org.opengauss.admin.common.core.domain.model.ops;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.vo.HostInfoVo;
import org.opengauss.admin.common.utils.excel.TagsConverter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Excel Host Record Entity Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ColumnWidth(15)
public class HostRecord {
    @ExcelProperty(value = "序号", index = 0)
    private Integer id;
    @ExcelProperty(value = "服务器名称", index = 1)
    private String name;
    @ExcelProperty(value = "内网IP", index = 2)
    @NotEmpty(message = "The Intranet IP address cannot be empty")
    private String privateIp;
    @ExcelProperty(value = "公网IP", index = 3)
    @NotNull(message = "The IP address cannot be empty")
    private String publicIp;
    @NotNull(message = "The IP address cannot be empty")
    @ExcelProperty(value = "端口号", index = 4)
    private Integer port;
    @ExcelProperty(value = "用户名称", index = 5)
    private String userName;
    @ExcelProperty(value = "用户密码", index = 6)
    @ToString.Exclude
    private String password;
    @ExcelProperty(value = "是否为管理员（是|否）", index = 7)
    private String isAdmin;
    @ExcelProperty(value = "标签", converter = TagsConverter.class, index = 8)
    private List<String> tags;
    @ExcelProperty(value = "备注", index = 9)
    private String remark;

    public OpsHostEntity toHostEntity(HostInfoVo hostInfoVo) {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setPublicIp(publicIp);
        hostEntity.setPrivateIp(privateIp);
        hostEntity.setPort(port);
        hostEntity.setHostname(hostInfoVo.getHostname());
        hostEntity.setRemark(remark);
        hostEntity.setIsRemember(Boolean.TRUE);
        hostEntity.setOs(hostInfoVo.getOs());
        hostEntity.setOsVersion(hostInfoVo.getOsVersion());
        hostEntity.setCpuArch(hostInfoVo.getCpuArch());
        hostEntity.setName(name);
        return hostEntity;
    }

    public OpsHostUserEntity toUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername(userName);
        hostUserEntity.setHostId(hostId);
        hostUserEntity.setSudo(Boolean.TRUE);
        return hostUserEntity;
    }

    public HostBody toHostBody() {
        HostBody hostBody = new HostBody();
        BeanUtil.copyProperties(this, hostBody);
        hostBody.setIsRemember(true);
        hostBody.setUsername(userName);
        return hostBody;
    }

    public ErrorHostRecord toErrorHostRecord() {
        ErrorHostRecord errorHostRecord = new ErrorHostRecord();
        BeanUtil.copyProperties(this, errorHostRecord);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:dd:MM:yyyy");
        String formattedDate = sdf.format(new Date());
        errorHostRecord.setTimestamp(formattedDate);
        return errorHostRecord;
    }
}
