package org.opengauss.admin.plugin.service.ops;

/**
 * @author lhf
 * @date 2023/2/14 14:28
 **/
public interface IHostService {
    boolean pathEmpty(String id, String path, String rootPassword);

    boolean portUsed(String id, Integer port, String rootPassword);
}
