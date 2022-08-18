package org.opengauss.admin.plugin.mapper.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lhf
 * @date 2022/11/5 09:47
 **/
@Mapper
public interface OpsBackupMapper extends BaseMapper<OpsBackupEntity> {
    Page<BackupVO> pageBackup(Page page, @Param("clusterId") String clusterId);
}
