package org.opengauss.admin.web.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.starblues.core.PluginInfo;
import com.gitee.starblues.core.PluginState;
import com.gitee.starblues.integration.operator.PluginOperator;
import com.gitee.starblues.integration.operator.upload.UploadParam;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.PluginConfigDataDto;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.enums.SysPluginStatus;
import org.opengauss.admin.common.enums.SysPluginTheme;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.file.FileUploadUtils;
import org.opengauss.admin.system.domain.SysPlugin;
import org.opengauss.admin.system.domain.SysPluginLogo;
import org.opengauss.admin.system.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@RestController
@RequestMapping("/system/plugins")
@Api(tags = "plugins")
@Slf4j
public class SystemPluginController extends BaseController {

    @Autowired
    private ISysPluginService sysPluginService;
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


    /**
     * get plugin list
     */
    @GetMapping("/list")
    @ApiOperation(value = "plugin list", notes = "plugin list")
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
     * get plugin count
     */
    @GetMapping("/count")
    @ApiOperation(value = "plugin count", notes = "plugin count")
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
    @ApiOperation("start plugin")
    @ApiImplicitParam(name = "id", value = "pluginId", paramType = "path", required = true)
    public AjaxResult start(@PathVariable("id") String id) {
        try {
            if (pluginOperator.start(id)) {
                sysPluginService.startByPluginId(id);
                return AjaxResult.success();
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_START_ERROR.code());
            }
        } catch (Exception e) {
            StringWriter errorsWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorsWriter));
            log.error(errorsWriter.toString());
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_START_ERROR.code(),e.getMessage());
        }


    }

    /**
     * stop plugin by id
     *
     * @param id plugin id
     */
    @Log(title = "plugins", businessType = BusinessType.STOP)
    @PostMapping("/stop/{id}")
    @ApiOperation("stop plugin")
    @ApiImplicitParam(name = "id", value = "pluginId", paramType = "path", required = true)
    public AjaxResult stop(@PathVariable("id") String id) {
        try {
            Integer count = sysMenuService.countMenuHasOtherPluginSubmenuByPluginId(id);
            if (count > 0) {
                return AjaxResult.error(ResponseCode.PLUGIN_MENU_HAS_OTHER_PLUGIN_SUBMENU_UNINSTALL_ERROR.code());
            }
            PluginInfo pluginInfo = pluginOperator.getPluginInfo(id);
//            File file = new
//            pluginInfo.getPluginDescriptor().getPluginPath()

            if (pluginOperator.stop(id)) {
                sysPluginService.stopByPluginId(id);
                return AjaxResult.success();
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_STOP_ERROR.code());
            }
        } catch (Exception e) {
            StringWriter errorsWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorsWriter));
            log.error(errorsWriter.toString());
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_STOP_ERROR.code(),e.getMessage());
        }
    }

    /**
     * upload and instal plugin
     * @param jarFile jarFile
     */
    @Log(title = "plugins", businessType = BusinessType.INSTALL)
    @PostMapping("/install")
    @ApiOperation("instal plugin")
    public AjaxResult install(@RequestParam("file") MultipartFile jarFile) {
        try {
            UploadParam uploadParam = UploadParam.byMultipartFile(jarFile)
                    .setBackOldPlugin(true)
                    .setStartPlugin(true)
                    .setUnpackPlugin(false);
            PluginInfo pluginInfo = pluginOperator.uploadPlugin(uploadParam);
            if (pluginInfo != null) {
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
                SysPlugin plugin = SysPlugin.builder().pluginId(pluginInfo.getPluginId())
                        .pluginDesc(pluginInfo.getPluginDescriptor().getDescription()).pluginDescEn(descriptionEn)
                        .bootstrapClass(pluginInfo.getPluginDescriptor().getPluginBootstrapClass())
                        .pluginProvider(pluginInfo.getPluginDescriptor().getProvider()).isNeedConfigured(isNeedConfigured)
                        .pluginType(pluginType).pluginVersion(pluginInfo.getPluginDescriptor().getPluginVersion())
                        .theme(theme).build();
                sysPluginService.save(plugin);
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
                }
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.code());
            }
        } catch (Exception e) {
            StringWriter errorsWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorsWriter));
            log.error(errorsWriter.toString());
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_INSTALL_ERROR.code());
        }
    }

    /**
     * uninstall plugin
     * If there are submenus added by other plugins under your own menu, you cannot uninstall them
     * @param id plugin id
     */
    @Log(title = "plugins", businessType = BusinessType.UNINSTALL)
    @PostMapping("/uninstall/{id}")
    @ApiOperation("uninstall plugin")
    @ApiImplicitParam(name = "id", value = "pluginId", paramType = "path", required = true)
    public AjaxResult uninstall(@PathVariable("id") String id) {
        try {
            Integer count = sysMenuService.countMenuHasOtherPluginSubmenuByPluginId(id);
            if (count > 0) {
                return AjaxResult.error(ResponseCode.PLUGIN_MENU_HAS_OTHER_PLUGIN_SUBMENU_UNINSTALL_ERROR.code());
            }
            pluginOperator.uninstall(id, true, true);
            return AjaxResult.success();
        } catch (Exception e) {
            StringWriter errorsWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorsWriter));
            log.error(errorsWriter.toString());
            return AjaxResult.error(ResponseCode.INTEGRATION_PLUGIN_UNINSTALL_ERROR.code(),e.getMessage());
        }
    }

    /**
     * get plugin info
     *
     * @param id plugin id
     */
    @PostMapping("/get/{id}")
    @ApiOperation("get info")
    @ApiImplicitParam(name = "id", value = "pluginId", paramType = "path", required = true)
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
    @ApiOperation("save configData")
    public AjaxResult savePluginConfigData(@RequestBody PluginConfigDataDto configDataDto) {
        sysPluginConfigDataService.savePluginConfigData(configDataDto.getPluginId(), configDataDto.getConfigData());
        return AjaxResult.success();
    }
}
