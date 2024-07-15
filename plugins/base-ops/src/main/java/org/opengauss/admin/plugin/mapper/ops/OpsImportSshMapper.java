package org.opengauss.admin.plugin.mapper.ops;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.entity.ops.OpsImportSshEntity;

import java.util.List;

@Mapper
public interface OpsImportSshMapper {
    /**
     * query port, password HostIdAndInstallUserId of installUserName by device publicIp
     *
     * @param publicIp,installUserName publicIp,installUserName
     * @return List<OpsImportSshEntity>
     */
    List<OpsImportSshEntity> queryHostInfo(@Param("installUserName") String installUserName, @Param("publicIp") String publicIp);
    /**
     * query clusterId of cluster by publicIp and databasePort
     *
     * @param publicIp,databasePort publicIp,databasePort
     * @return List<String>
     */
    List<String> checkPublicIpAndPort(@Param("publicIp") String publicIp, @Param("databasePort") String databasePort);
}
