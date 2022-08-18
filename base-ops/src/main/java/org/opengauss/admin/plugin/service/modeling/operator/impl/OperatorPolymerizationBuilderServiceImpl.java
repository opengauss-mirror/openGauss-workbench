package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songlei
 * @date 2022/8/15 16:35
 */
@Component("polymerization")
public class OperatorPolymerizationBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        // Polymerization region modify
        String oriSql = sqlObject.getPolymerizationRegion();

        JSONArray polymerizations = params.getJSONArray("polymerization");
        List<String> sqlPolymerization = new ArrayList<>();

        if (!polymerizations.isEmpty()) {
            for (int i = 0; i < polymerizations.size(); i++) {
                JSONObject polymerizationItem = polymerizations.getJSONObject(i);
                String way = polymerizationItem.getString("way");
                String field = polymerizationItem.getString("field");
                String alias = polymerizationItem.getString("alias");
                if (way != null && field !=null) {
                    sqlPolymerization.add(polymerizationConvert(way,field,alias));
                }
            }
            if (sqlPolymerization.size()>0) {
                oriSql += String.join(",",sqlPolymerization) + " ";
            }
        }

        sqlObject.setPolymerizationRegion(oriSql);

        return sqlObject;
    }

    private String polymerizationConvert(String way,String field,String alias) {
        String sql;
        switch (way) {
            case "max":
                sql = "max("+field+")";
                break;
            case "min":
                sql = "min("+field+")";
                break;
            case "avg":
                sql = "avg("+field+")";
                break;
            case "sum":
                sql = "sum("+field+")";
                break;
            default:
                sql = "";
                break;
        }

        if (alias!=null) {
            sql += " as "+alias;
        }

        return sql;
    }
}
