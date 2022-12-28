package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;

/**
 * @author lhf
 * @date 2022/11/5 09:48
 **/
public interface IOpsBackupService extends IService<OpsBackupEntity> {

    void backup(BackupInputDto backup);

    void recover(String id, RecoverInputDto recover);

    void del(String id);

    Page<BackupVO> pageBackup(Page page, String clusterId);
}
