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
 * HostController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/HostController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.ImportAsynInfo;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.OsSupportMap;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.excel.ImportAsynInfoUtils;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Host
 *
 * @author lhf
 * @date 2022/8/7 22:24
 **/
@RestController
@RequestMapping("/host")
public class HostController extends BaseController {

    @Autowired
    private IHostService hostService;

    private final HashMap<String, InputStream> fileStreamMap = new HashMap();

    @PostMapping
    public AjaxResult add(@RequestBody @Validated HostBody hostBody) {
        hostService.add(hostBody);
        return AjaxResult.success();
    }

    @PostMapping("/upload")
    public AjaxResult upload(@RequestPart @RequestParam("file") MultipartFile file) {
        ImportAsynInfo asynInfo = new ImportAsynInfo();
        String uuid = ImportAsynInfoUtils.createUUID(asynInfo);
        if (!file.isEmpty()) {
            try {
                fileStreamMap.put(uuid, file.getInputStream());
            } catch (IOException e) {
                throw new OpsException(e.getMessage());
            }
        } else {
            throw new OpsException("The file is empty.");
        }
        return AjaxResult.success(uuid);
    }

    /**
     * Invoke related file handling operations.
     *
     * @param uuid The unique identifier of the file.
     * @param isInvoke A flag that indicates whether to perform file processing;
     * 0 means to process, and non-zero means not to process.
     * @param currentLocale The current user's locale, used for internationalization support.
     * @return AjaxResult The result of the method execution,
     * which includes whether the operation was successful, along with related messages and data.
     */
    @PostMapping("/invokeFile")
    public AjaxResult invokeFile(@RequestParam String uuid, @RequestParam Integer isInvoke,
        @RequestParam String currentLocale) {
        if (isInvoke == 0) {
            hostService.invokeFile(uuid, fileStreamMap, currentLocale);
        }
        fileStreamMap.remove(uuid);
        return AjaxResult.success();
    }

    /**
     * Download the template file,
     * and download the corresponding template file based on the currently selected language.
     *
     * @param response HTTP response object, used to send files to the client.
     * @param currentLocale The current user's locale,
     * used to determine which version of the template to download.
     */
    @GetMapping("/downloadTemplate/{currentLocale}")
    public void downloadTemplate(HttpServletResponse response, @PathVariable String currentLocale) {
        hostService.downloadTemplate(response, currentLocale);
    }

    @GetMapping("/downloadErrorExcel/{uuid}")
    public void downloadErrorExcel(HttpServletResponse response, @PathVariable String uuid) {
        hostService.downloadErrorExcel(response, uuid);
        ImportAsynInfoUtils.deleteAsynInfo(uuid);
    }

    @GetMapping("get_import_plan")
    public AjaxResult getImportPlanByUuid(@RequestParam String uuid) {
        ImportAsynInfo asynInfo = ImportAsynInfoUtils.getAsynInfo(uuid);
        return asynInfo == null
            ? AjaxResult.error("The progress information has been fully obtained.")
            : asynInfo.getMsg() == null ? AjaxResult.success(asynInfo) : AjaxResult.error(asynInfo.getMsg());
    }

    @GetMapping("/listAll")
    public AjaxResult listAll(@RequestParam(value = "azId", required = false) String azId) {
        List<OpsHostEntity> hostEntityList = hostService.listAll(azId);
        return AjaxResult.success(hostEntityList);
    }

    /**
     * Query the list of hosts based on the AZ ID.
     *
     * @param name name
     * @param tagIds tagIds
     * @param os osName
     * @return page list
     */
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) String name,
        @RequestParam(value = "tagIds", required = false) Set<String> tagIds,
        @RequestParam(value = "os", required = false) String os) {
        IPage<OpsHostVO> page = hostService.pageHost(startPage(), name, tagIds, os);
        return getDataTable(page);
    }

    @PostMapping("/ping")
    public AjaxResult ping(@RequestBody @Validated HostBody hostBody) {
        boolean ping = hostService.ping(hostBody);
        return ping ? AjaxResult.success() : AjaxResult.error("Connectivity test failed");
    }

    /**
     * ping host.
     *
     * @param hostId host id
     * @param rootPassword pwd
     * @return ping status
     * @deprecated Use {@link #ping(String, String)} instead.
     */
    @Deprecated
    @GetMapping("/ping/{hostId}")
    public AjaxResult ping(@PathVariable String hostId,
        @RequestParam(value = "rootPassword", required = false) String rootPassword) {
        boolean ping = hostService.ping(hostId, rootPassword);
        return ping ? AjaxResult.success() : AjaxResult.error("Connectivity test failed");
    }

    @DeleteMapping("/{hostId}")
    public AjaxResult del(@PathVariable String hostId) {
        boolean del = hostService.del(hostId);
        return del ? AjaxResult.success() : AjaxResult.error("Failed to delete host");
    }

    /**
     * edit host.
     *
     * @param hostId host id
     * @param hostBody host body
     * @return edit status
     */
    @PutMapping("/{hostId}")
    public AjaxResult edit(@PathVariable String hostId, @RequestBody @Validated HostBody hostBody) {
        boolean edit = hostService.edit(hostId, hostBody);
        return edit ? AjaxResult.success() : AjaxResult.error("Failed to edit host");
    }

    @PostMapping("/ssh")
    public AjaxResult ssh(@RequestBody SSHBody sshBody) {
        hostService.ssh(sshBody);
        return AjaxResult.success();
    }

    /**
     * ssh host
     *
     * @param hostId host id
     * @param sshBody ss body
     * @return ssh status
     */
    @PostMapping("/ssh/{hostId}")
    public AjaxResult ssh(@PathVariable("hostId") String hostId, @RequestBody SSHBody sshBody) {
        hostService.ssh(hostId, sshBody);
        return AjaxResult.success();
    }

    /**
     * page monitor host lost
     *
     * @param hostIds host ids
     * @param businessId business id
     * @return monitor status
     */
    @PostMapping("/monitor")
    public AjaxResult pageMonitor(@RequestBody List<String> hostIds, @RequestParam String businessId) {
        return AjaxResult.success(hostService.pageMonitor(hostIds, businessId));
    }

    @GetMapping("/listSupportOsName")
    public AjaxResult listSupportOsName() {
        return AjaxResult.success(OsSupportMap.getSupportOsNameList());
    }
}
