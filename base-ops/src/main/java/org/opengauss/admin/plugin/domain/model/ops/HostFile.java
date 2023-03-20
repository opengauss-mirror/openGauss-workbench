package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.plugin.enums.ops.HostFileTypeEnum;
import lombok.Data;

import java.io.File;
import java.util.UUID;

/**
 * @author lhf
 * @date 2022/8/7 22:37
 **/
@Data
public class HostFile {

    private String name;

    private HostFileTypeEnum type;
    /**
     * size（byte）
     */
    private Long size;

    private String openGaussVersionNum;

    public static HostFile build(File file) {
        HostFile hostFile = new HostFile();
        String fileName = file.getName();
        HostFileTypeEnum fileType = file.isFile() ? HostFileTypeEnum.FILE : HostFileTypeEnum.DIRECTORY;
        long length = file.length();
        hostFile.setOpenGaussVersionNum(parseVersionNum(fileName));
        hostFile.setName(fileName);
        hostFile.setType(fileType);
        hostFile.setSize(length);

        return hostFile;
    }

    public static HostFile of(String filename, HostFileTypeEnum hostFileTypeEnum, long size) {
        HostFile hostFile = new HostFile();
        hostFile.setName(filename);
        hostFile.setType(hostFileTypeEnum);
        hostFile.setSize(size);
        return hostFile;
    }

    private static String parseVersionNum(String fileName) {
        if (StrUtil.isNotEmpty(fileName)){
            if (fileName.startsWith("openGauss-Lite-")){
                String[] split = fileName.split("-");
                if (split.length>=3){
                    return split[2];
                }else {
                    return null;
                }
            }else if (fileName.startsWith("openGauss-")){
                String[] split = fileName.split("-");
                if (split.length>=2){
                    return split[1];
                }else {
                    return null;
                }
            }else {
                //uuid
                return parseVersionNum(fileName.substring(fileName.indexOf("openGauss")));
            }
        }
        return null;
    }
}
