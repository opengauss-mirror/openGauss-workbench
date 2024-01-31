/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.service;

import java.util.List;
import org.opengauss.tun.domain.TrainingProgress;

/**
 * TrainingProgressService
 *
 * @author liu
 * @since 2023-12-20
 */
public interface TrainingProgressService {
    /**
     * getTrainingProgressByTunId
     *
     * @param id id
     * @return List<TrainingProgress>
     */
    List<TrainingProgress> getTrainingProgressByTunId(String id);

    /**
     * getMaxTpsByByTunId
     *
     * @param id id
     * @return TrainingProgress
     */
    TrainingProgress getMaxTpsByByTunId(String id);

    /**
     * selectInitialTpsRecord
     *
     * @param id id
     * @param paramType paramType
     * @return TrainingProgress
     */
    TrainingProgress selectInitialTpsRecord(String id, String paramType);
}
