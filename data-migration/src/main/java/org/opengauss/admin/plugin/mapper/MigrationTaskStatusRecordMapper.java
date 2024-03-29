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
 * MigrationTaskStatusRecordMapper.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/mapper/MigrationTaskStatusRecordMapper.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;

import java.util.List;

/**
* @author xielibo
* @date 2023/01/14 09:01
*/
@Mapper
public interface MigrationTaskStatusRecordMapper extends BaseMapper<MigrationTaskStatusRecord> {

    public List<MigrationTaskStatusRecord> selectByTaskId(@Param("taskId") Integer taskId);

}




