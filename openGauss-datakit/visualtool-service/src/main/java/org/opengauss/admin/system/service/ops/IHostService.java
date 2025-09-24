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
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IHostService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lhf
 * @date 2022/8/7 22:27
 **/
public interface IHostService extends IService<OpsHostEntity> {
    /**
     * Add host
     *
     * @param hostBody host information
     */
    boolean add(HostBody hostBody);

    /**
     * test connectivity
     *
     * @param hostBody host information
     * @return whether succeed
     */
    boolean ping(HostBody hostBody);

    /**
     * delete host
     *
     * @param hostId HostId
     * @return Whether the deletion is successful
     */
    boolean del(String hostId);

    /**
     * test connectivity
     *
     * @param hostId       host ID
     * @param rootPassword
     * @return whether succeed
     */
    boolean ping(String hostId, String rootPassword);

    /**
     * Edit host information
     *
     * @param hostId   host ID
     * @param hostBody host information
     * @return whether succeed
     */
    boolean edit(String hostId, HostBody hostBody);

    /**
     * Parse the Excel file.
     *
     * @param uuid The unique identifier of the file.
     * @param fileStreamMap Excel file
     * @param currentLocale current language
     */
    void invokeFile(String uuid, HashMap<String, InputStream> fileStreamMap, String currentLocale);

    /**
     * download template
     *
     * @param response HTTP response object
     * @param currentLocale current language
     */
    void downloadTemplate(HttpServletResponse response, String currentLocale);

    void downloadErrorExcel(HttpServletResponse response, String uuid);

    IPage<OpsHostVO> pageHost(Page page, String name, Set<String> tagIds, String os);

    void ssh(SSHBody sshBody);

    void ssh(String hostId, SSHBody sshBody);

    Map<String, String> mapOsByIps(Set<String> ipSet);

    List<OpsHostEntity> listAll(String azId);

    /**
     * page hosts monitor
     *
     * @param hostIds host ids
     * @param businessId business id
     * @return host status
     */
    Map<String, Object> pageMonitor(List<String> hostIds, String businessId);

    void updateHostOsVersion(OpsHostEntity opsHostEntity);

    OpsHostEntity getMappedHostEntityById(String hostId);

    /**
     * get host info
     *
     * @param hostIp host public Ip
     * @return host
     */
    OpsHostEntity getByPublicIp(String hostIp);

    /**
     * update host os name
     *
     * @param host host ip
     * @param osName os name
     */
    void updateHostOsName(String host, String osName);

    /**
     * update host os version
     *
     * @param host host ip
     * @param osVersion os version
     */
    void updateHostOsVersion(String host, String osVersion);

    /**
     * update host cpu info by host ip
     *
     * @param host host ip
     * @param cpuArch cpu arch
     * @param cpuCoreNum cpu core num
     * @param cpuFreq cpu freq
     */
    void updateHostCpu(String host, String cpuArch, String cpuCoreNum, String cpuFreq);
}
