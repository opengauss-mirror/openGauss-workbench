package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.enums.ops.HostFileTypeEnum;
import lombok.Data;

import java.io.File;

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

    public static HostFile build(File file) {
        HostFile hostFile = new HostFile();
        String fileName = file.getName();
        HostFileTypeEnum fileType = file.isFile() ? HostFileTypeEnum.FILE : HostFileTypeEnum.DIRECTORY;
        long length = file.length();

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
}
