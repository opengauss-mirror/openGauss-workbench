/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.plugin.controller.modeling;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationParamsEntity;
import org.opengauss.admin.plugin.service.modeling.IModelingVisualizationGeoFilesService;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationParamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Slf4j
@RestController
@RequestMapping("/modeling/dataflow/visualization/params")
public class ModelingVisualizationParamsController extends BaseController {

    @Autowired
    private IModelingVisualizationParamsService modelingVisualizationParamsService;
    @Autowired
    private IModelingVisualizationGeoFilesService modelingVisualizationGeoFilesService;

    @GetMapping("/getListByOperatorId/{operatorId}")
    public AjaxResult getListByOperatorId(@PathVariable String operatorId) {
        List<ModelingVisualizationParamsEntity> list = modelingVisualizationParamsService.getByOperatorId(operatorId);
        return AjaxResult.success(list);
    }

    /**
     * add
     */
    @RequestMapping("/add")
    public AjaxResult add(@RequestBody ModelingVisualizationParamsEntity modelingVisualizationParamsEntity) {
        return toAjax(modelingVisualizationParamsService.add(modelingVisualizationParamsEntity));
    }

    /**
     * delete
     */
    @DeleteMapping("/delete/{flowIds}")
    public AjaxResult delete(@PathVariable String[] flowIds) {
        return toAjax(modelingVisualizationParamsService.deleteByIds(flowIds));
    }


    /**
     * save
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody ModelingVisualizationParamsEntity modelingVisualizationParamsEntity) {
        return toAjax(modelingVisualizationParamsService.update(modelingVisualizationParamsEntity));
    }

    @PostMapping("/generateChart/{paramsId}")
    public AjaxResult generateChartByParamId(@PathVariable String paramsId) throws Exception {
        ModelingVisualizationParamsEntity params = modelingVisualizationParamsService.getById(paramsId);
        JSONObject paramsJson = JSONObject.parseObject(params.getParamsJson());

        AjaxResult ajax = AjaxResult.success();
        ajax.put("chartData", modelingVisualizationParamsService.generateChart(paramsJson));
        return ajax;
    }

    @PostMapping("/generateChart")
    public AjaxResult generateChartByJson(@RequestBody JSONObject params) {
        AjaxResult ajax = AjaxResult.success();
        try {
            ajax.put("chartData", JSONObject.parse((modelingVisualizationParamsService.generateChart(params))));
            if (Objects.equals(params.getJSONObject("paramsData").getString("chartType"), "scatter")) {
                //get geo config
                ModelingVisualizationGeoFilesEntity geoFile = modelingVisualizationGeoFilesService.getById(params.getJSONObject("paramsData").getJSONObject("location").getLong("commonValue"));
                JSONObject mapInfo = new JSONObject();
                mapInfo.put("name",geoFile.getRegisterName());
                mapInfo.put("data",JSONObject.parse(geoFile.getGeoJson()));
                ajax.put("mapData", mapInfo);
            }
        } catch (Exception e) {
            return AjaxResult.error("generate Chart error.Please check exception info : "+ e.getMessage());
        }

        return ajax;
    }

    @PostMapping("/uploadGeo")
    public AjaxResult upload(@RequestParam Long id, @RequestParam String name, @RequestParam("file") MultipartFile file) {
        AjaxResult result = AjaxResult.success();
        ModelingVisualizationGeoFilesEntity newFile = new ModelingVisualizationGeoFilesEntity();
        newFile.setDataFlowId(id);
        newFile.setName(file.getOriginalFilename());
        newFile.setRegisterName(name);
        InputStreamReader isr = null;
        InputStream in = null;
        StringBuilder str = new StringBuilder();
        try {
            in = file.getInputStream();
            isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String s;
            while ((s = br.readLine()) != null) {
                str.append(s);
            }
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("close input stream failed: " + e.getMessage());
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.error("close input stream reader failed: " + e.getMessage());
                }
            }
        }

        newFile.setGeoJson(str.toString());

        modelingVisualizationGeoFilesService.add(newFile);
        return result;
    }


    @GetMapping("/getGeo/{dataFlowId}")
    public AjaxResult getGeo(@PathVariable Long dataFlowId) {
        AjaxResult result = AjaxResult.success();
        result.put("geoFiles",modelingVisualizationGeoFilesService.selectByDataFlowId(dataFlowId));
        return result;
    }

    @DeleteMapping("/deleteGeo/{geoId}")
    public AjaxResult deleteGeo(@PathVariable Long geoId) {
        AjaxResult result = AjaxResult.success();
        modelingVisualizationGeoFilesService.removeById(geoId);
        return result;
    }

}

