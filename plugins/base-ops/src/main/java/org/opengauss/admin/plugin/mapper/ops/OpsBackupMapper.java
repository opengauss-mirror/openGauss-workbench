/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsBackupMapper.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/mapper/ops/OpsBackupMapper.java
 *
 * -------------------------------------------------------------------------
 */

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
