package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MainTaskEnvErrorHost;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MainTaskEnvErrorHostService extends IService<MainTaskEnvErrorHost> {


    void saveRecord(Integer mainTaskId, MigrationTaskHostRef hostRef);

    void deleteByMainTaskId(Integer mainTaskId);

    MainTaskEnvErrorHost getOneByMainTaskId(Integer mainTaskId);
}
