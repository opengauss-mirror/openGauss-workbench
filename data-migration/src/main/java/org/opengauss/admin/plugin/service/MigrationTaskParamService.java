package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskParam;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskParamService extends IService<MigrationTaskParam> {

    List<MigrationTaskParam> selectByTaskId(Integer taskId);

    void deleteByMainTaskId(Integer mainTaskId);
}
