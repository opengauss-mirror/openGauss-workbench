package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

/**
 * @author lhf
 * @date 2023/1/13 13:31
 **/
@Data
public class JdbcDbClusterNodeInputDto {
    private String name;
    private String url;
    private String username;
    private String password;
    private String remark;
    private DeployTypeEnum deployType;

    public static JdbcDbClusterNodeInputDto of(String name, String url, String username, String password) {
        JdbcDbClusterNodeInputDto jdbcDbClusterNodeInputDto = new JdbcDbClusterNodeInputDto();
        jdbcDbClusterNodeInputDto.setName(name);
        jdbcDbClusterNodeInputDto.setUrl(url);
        jdbcDbClusterNodeInputDto.setUsername(username);
        jdbcDbClusterNodeInputDto.setPassword(password);
        return jdbcDbClusterNodeInputDto;
    }
}
