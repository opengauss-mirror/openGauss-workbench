package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsPackageManagerMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author lhf
 * @date 2022/12/11 16:14
 **/
@Slf4j
@Service
public class OpsPackageManagerService extends ServiceImpl<OpsPackageManagerMapper, OpsPackageManagerEntity> implements IOpsPackageManagerService {
    @Override
    public String getCpuArchByPackagePath(String installPackagePath, OpenGaussVersionEnum version) {
        if (!FileUtil.exist(installPackagePath)){
            throw new OpsException("The installation package does not exist");
        }

        if (version == OpenGaussVersionEnum.ENTERPRISE){
            return getEnterprisePackageCpuArch(installPackagePath);
        }else if (version == OpenGaussVersionEnum.MINIMAL_LIST){
            return getMinimalListPackageCpuArch(installPackagePath);
        }else if (version == OpenGaussVersionEnum.LITE){
            return getLitePackageCpuArch(installPackagePath);
        }else {
            throw new OpsException("An unsupported version");
        }
    }

    private String getLitePackageCpuArch(String installPackagePath) {
        if (installPackagePath.contains("aarch64")){
            return "aarch64";
        }

        if (installPackagePath.contains("x86_64")){
            return "x86_64";
        }

        return null;
    }

    private String getMinimalListPackageCpuArch(String installPackagePath) {
        String tempFolder = "temp-minimal-"+ StrUtil.uuid();
        String tempAbsoluteFolder = FileUtil.getParent(installPackagePath,1) + File.separator + tempFolder;
        FileUtil.mkdir(tempAbsoluteFolder);

        try {
            Process tar = Runtime.getRuntime().exec("tar -jxf " + installPackagePath + " -C " + tempAbsoluteFolder);
            int exitCode = tar.waitFor();
            if (0!=exitCode){
                throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to decompress the installation package",e);
            throw new OpsException("Failed to decompress the installation package");
        }

        try {
            Process file = Runtime.getRuntime().exec("file " + tempAbsoluteFolder + File.separator + "bin" + File.separator + "gs_ctl");
            int exitCode = file.waitFor();
            if (0!=exitCode){
                throw new OpsException("Failed to get cpu arch with exitCode " + exitCode);
            }
            String res = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);
            if (StrUtil.isEmpty(res)){
                log.error("Failed to get cpu arch:{}");
                throw new OpsException("Failed to get cpu arch");
            }else {
                final String[] split = res.split(",");
                if (split.length<2){
                    log.error("Failed to get cpu arch:{}",res);
                    throw new OpsException("Failed to get cpu arch");
                }

                String trim = split[1].trim();
                final String[] s = trim.split(" ");

                if (s.length==1){
                    return s[0];
                }if (s.length>1){
                    return s[s.length-1];
                }
            }
        }catch (IOException | InterruptedException e){
            log.error("Failed to get cpu arch",e);
            throw new OpsException("Failed to get cpu arch");
        }
        return null;
    }

    private String getEnterprisePackageCpuArch(String installPackagePath) {
        String version = getVersion(installPackagePath);
        String system = getSystem(installPackagePath);
        log.info("version:{}",version);
        String tempFolder = "temp-enterprise-"+ StrUtil.uuid();
        String tempAbsoluteFolder = FileUtil.getParent(installPackagePath,1) + File.separator + tempFolder;
        FileUtil.mkdir(tempAbsoluteFolder);
        try {
            String command = "tar -xvf " + installPackagePath + " -C " + tempAbsoluteFolder;
            Process tar = Runtime.getRuntime().exec(command);
            int exitCode = tar.waitFor();
            if (0!=exitCode){
                log.error("command:{}",command);
                throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to decompress the installation package",e);
            throw new OpsException("Failed to decompress the installation package");
        }

        try {
            String command = "tar -xvf " +  tempAbsoluteFolder+File.separator + "openGauss-" +version+ "-"+system+"-64bit-cm.tar.gz " + " -C " + tempAbsoluteFolder;
            Process tar = Runtime.getRuntime().exec(command);
            int exitCode = tar.waitFor();
            if (0!=exitCode){
                log.error("command:{}",command);
                throw new OpsException("Failed to decompress the installation package with exitCode " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to decompress the installation package",e);
            throw new OpsException("Failed to decompress the installation package");
        }

        try {
            String command = "file " + tempAbsoluteFolder + File.separator + "bin" + File.separator + "cm_ctl";
            Process file = Runtime.getRuntime().exec(command);
            int exitCode = file.waitFor();
            if (0!=exitCode){
                log.error("command:{}",command);
                throw new OpsException("Failed to get cpu arch with exitCode " + exitCode);
            }
            String res = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);
            if (StrUtil.isEmpty(res)){
                log.error("Failed to get cpu arch:{}");
                throw new OpsException("Failed to get cpu arch");
            }else {
                final String[] split = res.split(",");
                if (split.length<2){
                    log.error("Failed to get cpu arch:{}",res);
                    throw new OpsException("Failed to get cpu arch");
                }

                String trim = split[1].trim();
                final String[] s = trim.split(" ");

                if (s.length==1){
                    return s[0];
                }if (s.length>1){
                    return s[s.length-1];
                }
            }
        }catch (IOException | InterruptedException e){
            log.error("Failed to get cpu arch",e);
            throw new OpsException("Failed to get cpu arch");
        }

        return null;
    }

    private String getSystem(String installPackagePath) {
        String packageName = installPackagePath.substring(installPackagePath.lastIndexOf("/"));
        return packageName.split("-")[2];
    }

    private String getVersion(String installPackagePath) {
        String packageName = installPackagePath.substring(installPackagePath.lastIndexOf("/"));
        return packageName.split("-")[1];
    }
}
