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
 * MigrationMainTaskMapper.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/mapper/MigrationMainTaskMapper.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationMainTask;
import org.opengauss.admin.plugin.dto.MigrationMainTaskDto;

import java.util.List;


/**
 * Task Mapper
 *
 * @author xielibo
 */
@Mapper
public interface MigrationMainTaskMapper extends BaseMapper<MigrationMainTask> {


    /**
     * Query the task list by page
     *
     * @param page  page
     * @param task task
     */
    public IPage<MigrationMainTask> selectTaskPage(IPage<MigrationMainTask> page, @Param("entity") MigrationMainTaskDto task);

    /**
     * Query the task list
     *
     * @param task task
     */
    public List<MigrationMainTask> selectTaskList(MigrationMainTaskDto task);

    public List<String> selectCreateUsers();
}
