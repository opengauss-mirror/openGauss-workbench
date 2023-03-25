package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
public interface IOpsPackageManagerService extends IService<OpsPackageManagerEntity> {
    String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version);

    void savePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    void updatePackage(OpsPackageManagerEntity pkg, Integer userId) throws OpsException;

    void delPackage(String id);

    OpsPackageVO analysisPkg(String pkgName, String pkgType);

    UploadInfo upload(MultipartFile file, Integer userId) throws OpsException;

    boolean deletePkgTar(String path, String id);

    String getSysUploadPath(Integer userId);

    boolean checkUploadPath(String path, Integer userId);
}
