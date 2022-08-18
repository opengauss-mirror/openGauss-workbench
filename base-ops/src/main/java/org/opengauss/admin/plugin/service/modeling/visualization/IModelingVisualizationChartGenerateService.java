package org.opengauss.admin.plugin.service.modeling.visualization;

import com.alibaba.fastjson.JSONObject;

import java.sql.SQLException;

/**
* @author LZW
*/
public interface IModelingVisualizationChartGenerateService {

     void setFullParams(JSONObject fullParams);
     String generate() throws SQLException, ClassNotFoundException;

}
