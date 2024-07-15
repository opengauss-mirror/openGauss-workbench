package org.opengauss.admin.common.core.domain.model.ops;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.vo.HostInfoVo;
import org.opengauss.admin.common.utils.excel.IsAdminConverter;
import org.opengauss.admin.common.utils.excel.TagsConverter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
public class HostRecord {
    @ExcelProperty("序号")
    private Integer id;
    @ExcelProperty("服务器名称")
    private String name;
    @ExcelProperty("内网IP")
    @NotEmpty(message = "The Intranet IP address cannot be empty")
    private String privateIp;
    @ExcelProperty("公网IP")
    @NotNull(message = "The IP address cannot be empty")
    private String publicIp;
    @NotNull(message = "The IP address cannot be empty")
    @ExcelProperty("端口号")
    private Integer port;
    @ExcelProperty("用户名称")
    private String userName;
    @ExcelProperty("用户密码")
    @ToString.Exclude
    private String password;
    @ExcelProperty(value = "是否为管理员", converter = IsAdminConverter.class)
    private Integer isAdmin;
    @ExcelProperty(value = "标签", converter = TagsConverter.class)
    private List<String> tags;
    @ExcelProperty("备注")
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
