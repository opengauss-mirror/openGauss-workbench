package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskGlobalParamService extends IService<MigrationTaskGlobalParam> {

    List<MigrationTaskGlobalParam> selectByMainTaskId(Integer mainTaskId);

    void deleteByMainTaskId(Integer mainTaskId);
}
