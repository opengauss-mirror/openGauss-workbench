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
 * IHostService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IHostService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import java.util.List;

/**
 * @author lhf
 * @date 2023/2/14 14:28
 **/
public interface IHostService {
    boolean pathEmpty(String id, String path, String rootPassword);

    boolean portUsed(String id, Integer port, String rootPassword);

    boolean fileExist(String id, String file, String rootPassword);

    /**
     * query lun path by nvme protocol
     *
     * @param id host id
     * @param rootPassword root password
     * @return list of query result
     */
    List<String> nvmeLunQuery(String id, String rootPassword);

    /**
     * query lun path by scsi protocol
     *
     * @param id host id
     * @param rootPassword root password
     * @return list of query result
     */
    List<String> scsiLunQuery(String id, String rootPassword);
    /**
     * query lun path
     *
     * @param id host id
     * @param rootPassword root password
     * @return list of query result
     */
    List<String> multiPathQuery(String id, String rootPassword);
}
