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
 * SystemPluginController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SystemPluginController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.starblues.core.DefaultPluginManager;
import com.gitee.starblues.core.PluginInfo;
import com.gitee.starblues.core.PluginState;
import com.gitee.starblues.core.RealizeProvider;
import com.gitee.starblues.core.exception.PluginException;
import com.gitee.starblues.integration.IntegrationConfiguration;
import com.gitee.starblues.integration.operator.PluginOperator;
import com.gitee.starblues.integration.operator.upload.UploadParam;
import com.gitee.starblues.spring.extract.ExtractCoordinate;
import com.gitee.starblues.spring.extract.ExtractFactory;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.config.SystemConfig;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.PluginConfigDataDto;
import org.opengauss.admin.common.core.dto.PluginDownloadDTO;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.PluginLicenseType;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.enums.SysPluginStatus;
import org.opengauss.admin.common.enums.SysPluginDownloadStatus;
import org.opengauss.admin.common.enums.SysPluginTheme;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.file.FileUploadUtils;
import org.opengauss.admin.system.domain.SysPlugin;
import org.opengauss.admin.system.domain.SysPluginLogo;
import org.opengauss.admin.system.plugin.beans.PluginExtensionInfoDto;
import org.opengauss.admin.system.plugin.extract.PluginExtensionInfoExtract;
import org.opengauss.admin.system.service.ISysPluginConfigDataService;
import org.opengauss.admin.system.service.ISysPluginConfigService;
import org.opengauss.admin.system.service.ISysPluginRepositoryService;
import org.opengauss.admin.system.service.ISysPluginService;
import org.opengauss.admin.system.service.ISysMenuService;
import org.opengauss.admin.system.service.ISysPluginLogoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * user profile controller
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RestController
@RequestMapping("/system/plugins")
@Slf4j
public class SystemPluginController extends BaseController {
    @Autowired
    private ISysPluginService sysPluginService;
    @Autowired
    private ISysPluginRepositoryService sysPluginRepositoryService;
    @Autowired
    private PluginOperator pluginOperator;
    @Autowired
    private ISysPluginConfigService sysPluginConfigService;
    @Autowired
    private ISysPluginConfigDataService sysPluginConfigDataService;
    @Autowired
    private ISysMenuService sysMenuService;
    @Autowired
    private ISysPluginLogoService iSysPluginLogoService;
    @Autowired
    private ExtractFactory extractFactory;
    @Autowired
    private RealizeProvider realizeProvider;
    @Autowired
    private IntegrationConfiguration configuration;

    private final String baseOpsId = "base-ops";


    /**
     * get plugin list
     */
    @GetMapping("/list")
    public TableDataInfo list(SysPlugin sysPlugin) {
        IPage<SysPlugin> list = sysPluginService.selectList(startPage(), sysPlugin);
        list.getRecords().forEach(d -> {
            PluginInfo pluginInfo = pluginOperator.getPluginInfo(d.getPluginId());
            if (pluginInfo != null && pluginInfo.getPluginState().toString().equals(PluginState.STARTED.toString())) {
                d.setPluginStatus(SysPluginStatus.START.getCode());
            } else {
                d.setPluginStatus(SysPluginStatus.DISABLE.getCode());
            }
            SysPluginLogo plugin = iSysPluginLogoService.getByPluginId(d.getPluginId());
            if (plugin != null) {
                d.setLogoPath(plugin.getLogoPath());
            }
        });
        return getDataTable(list);
    }

    /**
     * get plugin extension list
     */
    @GetMapping("/extensions/list")
    public TableDataInfo listExtensionInfo(SysPlugin sysPlugin) {
        IPage<SysPlugin> page = sysPluginService.selectList(startPage(), sysPlugin);
        List<PluginExtensionInfoDto> list = new ArrayList<>();
        page.getRecords().forEach(d -> {
            PluginExtensionInfoDto dto = new PluginExtensionInfoDto();
            dto.setPluginId(d.getPluginId());
            dto.setPluginLicenseType(PluginLicenseType.FREE);
            dto.setPluginName(d.getPluginId());
            try {
                ExtractCoordinate coordinate = ExtractCoordinate.build(d.getPluginId());
                PluginExtensionInfoExtract noticeExtract = extractFactory.getExtractByCoordinate(coordinate);
                PluginExtensionInfoDto extensionInfo = noticeExtract.getPluginExtensionInfo();
                BeanUtils.copyProperties(extensionInfo, dto);
            } catch (RuntimeException exception) {
                log.warn("Query plugin:" + d.getPluginId() + " extension info error");
            }
            list.add(dto);
        });
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(ResponseCode.SUCCESS.code());
        rspData.setMsg(ResponseCode.SUCCESS.msg());
        rspData.setRows(list);
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * get plugin count
     */
    @GetMapping("/count")
    public AjaxResult installCount() {
        return AjaxResult.success(sysPluginService.count());
    }

    @GetMapping("/listContent")
    public AjaxResult listContent() {
        return AjaxResult.success(pluginOperator.getPluginInfo());
    }

    /**
     * start plugin
     *
     * @param id plugin id
     */
    @Log(title = "plugins", businessType = BusinessType.START)
    @PostMapping("/start/{id}")
    public AjaxResult start(@PathVariable("id") String id) {
        try {
            if (pluginOperator.start(id)) {
                sysPluginService.startByPluginId(id);
                return AjaxResult.success();
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_START_ERROR.code());
            }
        } catch (Exception e) {
            log.error("start plugins [{}] failed ", id, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_START_ERROR.code(), e.getMessage());
        }
    }

    /**
     * stop plugin by id
     *
     * @param id plugin id
     */
    @Log(title = "plugins", businessType = BusinessType.STOP)
    @PostMapping("/stop/{id}")
    public AjaxResult stop(@PathVariable("id") String id) {
        try {
            Integer count = sysMenuService.countMenuHasOtherEnablePluginSubmenuByPluginId(id);
            if (count > 0) {
                return AjaxResult.error(ResponseCode.PLUGIN_MENU_HAS_OTHER_PLUGIN_SUBMENU_UNINSTALL_ERROR.code());
            }
            if (pluginOperator.stop(id)) {
                sysPluginService.stopByPluginId(id);
                return AjaxResult.success();
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_STOP_ERROR.code());
            }
        } catch (Exception e) {
            log.error("stop plugins [{}] failed ", id, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_STOP_ERROR.code(), e.getMessage());
        }
    }

    /**
     * upload and instal plugin offline
     *
     * @param jarFile jarFile
     */
    @Log(title = "plugins", businessType = BusinessType.INSTALL)
    @PostMapping("/offline_install")
    public AjaxResult install(@RequestParam("file") MultipartFile jarFile) {
        String pluginId = "";
        try {
            preInstall(jarFile);
            UploadParam uploadParam = UploadParam.byMultipartFile(jarFile)
                    .setBackOldPlugin(true)
                    .setStartPlugin(true)
                    .setUnpackPlugin(false);
            PluginInfo pluginInfo = pluginOperator.uploadPlugin(uploadParam);
            pluginId = pluginInfo.getPluginId();
            if (!pluginInfo.getPluginDescriptor().getPluginVersion()
                    .equals(sysPluginRepositoryService.getCurrentVersion())) {
                pluginOperator.uninstall(pluginInfo.getPluginId(), true, true);
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_VERSION_ERROR.msg()
                    + sysPluginRepositoryService.getCurrentVersion());
            }
            Map<String, Object> result;
            if ((result = updateSystemByPluginInfo(pluginInfo)) != null) {
                sysPluginRepositoryService.updatePluginDownloadStatus(
                        List.of(pluginInfo.getPluginId()), SysPluginDownloadStatus.DOWNLOADED_STATUS.getCode());
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.code());
            }
        } catch (Exception e) {
            log.error("offline_install plugins [{}] failed ", pluginId, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.msg() + ":" + e.getMessage());
        }
    }

    /**
     * getFileNameFromUrl
     *
     * @param url pluginDownloadUrl
     * @return jarFile name
     */
    private String getFileNameFromUrl(String url) {
        try {
            URL u = new URL(url);
            String path = u.getPath();
            return path.substring(path.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
    }

    /**
     * upload and instal plugin online
     *
     * @param dto dto
     */
    @Log(title = "plugins", businessType = BusinessType.INSTALL)
    @PostMapping("/online_install")
    public AjaxResult onlineInstall(@RequestBody PluginDownloadDTO dto) {
        String pluginUrl = dto.getPluginUrl();
        String wsBusinessId = dto.getWsBusinessId();
        String fileName = getFileNameFromUrl(pluginUrl);
        log.info("wsBusinessId is {} in onlineInstall", wsBusinessId);
        sysPluginRepositoryService.saveOnlinePackage(pluginUrl, fileName, wsBusinessId);
        String pluginPathString = SystemConfig.getPluginPath() + File.separator + getFileNameFromUrl(pluginUrl);
        Path pluginPath = Paths.get(pluginPathString);
        PluginInfo pluginInfo = pluginOperator.install(pluginPath, false);
        try {
            Map<String, Object> result;
            if ((result = updateSystemByPluginInfo(pluginInfo)) != null) {
                sysPluginRepositoryService.updatePluginDownloadStatus(
                        List.of(pluginInfo.getPluginId()), SysPluginDownloadStatus.DOWNLOADED_STATUS.getCode());
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.code());
            }
        } catch (Exception e) {
            log.error("online_install plugins [{}] failed ", fileName, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.msg() + ":" + e.getMessage());
        }
    }

    /**
     * Update plugin service info.
     * @param pluginInfo the plugin info.
     * @return addition map attr.
     */
    public Map<String, Object> updateSystemByPluginInfo(PluginInfo pluginInfo) {
        if (pluginInfo == null) {
            return null;
        }
        String logo = null;
        Integer pluginType = null;
        Integer isNeedConfigured = null;
        String theme = null;
        String configAttrs = null;
        String descriptionEn = null;
        if (pluginInfo.getExtensionInfo() != null) {
            logo = MapUtil.getStr(pluginInfo.getExtensionInfo(), "logo");
            descriptionEn = MapUtil.getStr(pluginInfo.getExtensionInfo(), "descriptionEn");
            pluginType = MapUtil.getInt(pluginInfo.getExtensionInfo(), "pluginType");
            isNeedConfigured = MapUtil.getInt(pluginInfo.getExtensionInfo(), "isNeedConfigured");
            theme = MapUtil.getStr(pluginInfo.getExtensionInfo(), "theme");
            configAttrs = MapUtil.getStr(pluginInfo.getExtensionInfo(), "configAttrs");

            if (!SysPluginTheme.DARK.getCode().equals(theme) && !SysPluginTheme.LIGHT.getCode().equals(theme)) {
                theme = null;
            }
            if (StringUtils.isNotBlank(logo)) {
                logo = Base64.decodeStr(logo);
                logo = FileUploadUtils.writeMenuSvgIcon(logo);
            }
        }
        SysPlugin plugin = sysPluginService.getByPluginId(pluginInfo.getPluginId());
        if (plugin == null) {
            plugin = SysPlugin.builder().pluginId(pluginInfo.getPluginId())
                    .pluginDesc(pluginInfo.getPluginDescriptor().getDescription()).pluginDescEn(descriptionEn)
                    .bootstrapClass(pluginInfo.getPluginDescriptor().getPluginBootstrapClass())
                    .pluginProvider(pluginInfo.getPluginDescriptor().getProvider()).isNeedConfigured(isNeedConfigured)
                    .pluginType(pluginType).pluginVersion(pluginInfo.getPluginDescriptor().getPluginVersion())
                    .theme(theme).build();
            setPluginStatus(plugin, pluginInfo);
            sysPluginService.save(plugin);
        } else {
            // update plugin version,desc,provider
            plugin.setPluginVersion(pluginInfo.getPluginDescriptor().getPluginVersion());
            plugin.setPluginDesc(pluginInfo.getPluginDescriptor().getDescription());
            plugin.setPluginDescEn(descriptionEn);
            plugin.setPluginProvider(pluginInfo.getPluginDescriptor().getProvider());
            setPluginStatus(plugin, pluginInfo);
            LambdaQueryWrapper<SysPlugin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysPlugin::getPluginId, pluginInfo.getPluginId());
            sysPluginService.update(plugin, queryWrapper);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("isNeedConfigured", isNeedConfigured);
        result.put("pluginId", pluginInfo.getPluginId());
        if (isNeedConfigured != null && isNeedConfigured == 1) {
            sysPluginConfigService.savePluginConfig(pluginInfo.getPluginId(), configAttrs);
            result.put("configAttrs", configAttrs);
            String alreadyData = sysPluginConfigDataService.getDataByPluginId(pluginInfo.getPluginId());
            if (StringUtils.isNotBlank(alreadyData)) {
                result.put("configData", alreadyData);
            }
        }
        if (StringUtils.isNotBlank(theme)) {
            sysMenuService.updatePluginMenuTheme(pluginInfo.getPluginId(), theme);
        }
        if (StringUtils.isNotBlank(logo)) {
            sysMenuService.updatePluginMenuIcon(pluginInfo.getPluginId(), logo);
            iSysPluginLogoService.savePluginConfig(pluginInfo.getPluginId(), logo);
            // refresh father logo
            sysMenuService.updatePluginFatherMenuIcon(pluginInfo.getPluginId(), logo);
        }
        return result;
    }

    private void setPluginStatus(SysPlugin plugin, PluginInfo pluginInfo) {
        if (PluginState.STARTED.equals(pluginInfo.getPluginState())) {
            plugin.setPluginStatus(SysPluginStatus.START.getCode());
        } else {
            plugin.setPluginStatus(SysPluginStatus.DISABLE.getCode());
        }
    }

    /**
     * uninstall plugin If there are submenus added by other plugins under your own menu, you cannot uninstall them
     *
     * @param id plugin id
     */
    @Log(title = "plugins", businessType = BusinessType.UNINSTALL)
    @PostMapping("/uninstall/{id}")
    public AjaxResult uninstall(@PathVariable("id") String id) {
        try {
            Integer count = sysMenuService.countMenuHasOtherPluginSubmenuByPluginId(id);
            if (count > 0) {
                return AjaxResult.error(ResponseCode.PLUGIN_MENU_HAS_OTHER_PLUGIN_SUBMENU_UNINSTALL_ERROR.code());
            }
            pluginOperator.uninstall(id, true, true);
            sysPluginRepositoryService.updatePluginDownloadStatus(List.of(id),
                SysPluginDownloadStatus.UN_DOWNLOAD_STATUS.getCode());
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("uninstall plugin failed {} ", id, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_UNINSTALL_ERROR.code(), e.getMessage());
        } catch (Error e) {
            log.error("uninstall plugin error {} ", id, e);
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_UNINSTALL_ERROR.code(), e.getMessage());
        }
    }

    /**
     * get plugin info
     *
     * @param id plugin id
     */
    @PostMapping("/get/{id}")
    public AjaxResult get(@PathVariable("id") String id) {
        try {
            return AjaxResult.success(pluginOperator.getPluginInfo(id));
        } catch (Exception e) {
            log.error(e.getMessage());
            return AjaxResult.error();
        }
    }


    /**
     * save plugin config content
     */
    @PostMapping("/pluginConfigData")
    public AjaxResult savePluginConfigData(@RequestBody PluginConfigDataDto configDataDto) {
        sysPluginConfigDataService.savePluginConfigData(configDataDto.getPluginId(), configDataDto.getConfigData());
        return AjaxResult.success();
    }

    private void preInstall(MultipartFile jarFile) {
        Path duplicateFilePath = getPluginPathWithDuplicateFileName(getPluginPaths(), jarFile);
        if (duplicateFilePath == null) {
            return;
        }
        if (isPluginLoaded(duplicateFilePath)) {
            return;
        }
        if (!removePluginInstallationPackage(duplicateFilePath)) {
            throw new PluginException(
                    "A file with the same name exists in the system plugin directory and failed to be deleted.");
        }
    }

    private List<Path> getPluginPaths() {
        return realizeProvider.getPluginScanner().scan(
                new DefaultPluginManager(realizeProvider, configuration).getPluginsRoots());
    }

    private Path getPluginPathWithDuplicateFileName(List<Path> pluginPaths, MultipartFile jarFile) {
        String jarFileName = jarFile.getOriginalFilename();
        Path result = null;

        for (Path path : pluginPaths) {
            String fileName = path.getFileName().toString();
            if (fileName.equals(jarFileName)) {
                result = path;
            }
        }

        return result;
    }

    private boolean isPluginLoaded(Path pluginPath) {
        return pluginOperator.getPluginInfo(pluginOperator.parse(pluginPath).getPluginId()) != null;
    }

    /**
     * get unload plugins info list
     */
    @GetMapping("/isBaseOpsStart")
    public AjaxResult getBaseOpsStatus() {
        if (!sysPluginService.getPluginList().contains(baseOpsId)) {
            return AjaxResult.success(false);
        }
        if (pluginOperator.getPluginInfo(baseOpsId).getPluginState().equals(PluginState.STOPPED)) {
            return AjaxResult.success(false);
        }
        return AjaxResult.success(true);
    }

    private boolean removePluginInstallationPackage(Path pluginPath) {
        try {
            Files.delete(pluginPath);
            log.info("Plugin installation package deleted successfully: {}", pluginPath);
        } catch (IOException e) {
            log.error("Failed to delete plugin installation package: {}", pluginPath, e);
            return false;
        }
        return true;
    }

    /**
     * get unload plugins info list
     */
    @GetMapping("/unloadPluginsInfo")
    public AjaxResult getUnloadPluginInfosList() {
        try {
            // 获取未下载插件的plugin_id列表
            List<String> unloadedPluginIds = sysPluginRepositoryService.getUnloadPluginIdsList();

            return AjaxResult.success(unloadedPluginIds);
        } catch (Exception e) {
            StringWriter errorsWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorsWriter));
            log.error(errorsWriter.toString());
            return AjaxResult.error(ResponseCode.INTEGRATION_UNLOAD_PLUGIN_INFO_QUERY_ERROR.msg()
                + ":" + e.getMessage());
        }
    }

    /**
     * get plugin info
     *
     * @param pluginId pluginVersion
     */
    @PostMapping("/getUnloadPluginUrl")
    public AjaxResult getUnloadPluginUrl(@RequestParam("pluginId") String pluginId) {
        String pluginVersion = sysPluginRepositoryService.getCurrentVersion();
        String unloadPluginUrl = sysPluginRepositoryService
            .getPluginUrlByIdAndVersion(pluginId, pluginVersion);
        if (unloadPluginUrl == null) {
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_URL_QUERY_ERROR.msg());
        }
        return AjaxResult.success(unloadPluginUrl);
    }
}
