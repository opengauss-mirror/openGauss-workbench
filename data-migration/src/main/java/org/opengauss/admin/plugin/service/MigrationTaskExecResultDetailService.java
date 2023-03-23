package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationTaskExecResultDetail;

import java.util.List;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 */
public interface MigrationTaskExecResultDetailService extends IService<MigrationTaskExecResultDetail> {

    void saveOrUpdateByTaskId(Integer taskId, String process, Integer processType);

    MigrationTaskExecResultDetail getByTaskIdAndProcessType(Integer taskId, Integer processType);

    void deleteByTaskIds(List<Integer> taskIds);
}
