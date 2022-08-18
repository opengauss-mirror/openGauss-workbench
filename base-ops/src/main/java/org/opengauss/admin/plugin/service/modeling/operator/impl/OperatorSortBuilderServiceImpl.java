package org.opengauss.admin.plugin.service.modeling.operator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author songlei
 * @date 2022/8/15 16:35
 */
@Component("sort")
public class OperatorSortBuilderServiceImpl extends BaseBuilderServiceImpl {

    @Override
    public ModelingDataFlowSqlObject getOperatorSql(JSONObject params, ModelingDataFlowSqlObject sqlObject){
        String oriSql = sqlObject.getOrderByRegion();

        JSONArray sorts = params.getJSONArray("sorts");
        List<String> sqlSorts = new ArrayList<>();

        if (!sorts.isEmpty()) {
            for (int i = 0; i < sorts.size(); i++) {
                JSONObject sortItem = sorts.getJSONObject(i);
                String sortField = sortItem.getString("field");
                String sortValue = sortItem.getString("value");
                if (sortField != null && sortValue !=null) {
                    sqlSorts.add(sortConvert(sortField,sortValue));
                }
            }
            if (sqlSorts.size()>0) {
                oriSql = "order by ";
                oriSql += String.join(",",sqlSorts) + " ";
            }
        }

        sqlObject.setOrderByRegion(oriSql);

        return sqlObject;
    }

    private static final HashMap<String,String> SORT_CONVERT = new HashMap<String,String>(){{
        this.put("asc","asc");
        this.put("desc","desc");
    }};

    private String sortConvert(String field,String value)
    {
        switch (value) {
            case "py_asc":
                return "convert_to("+field+",'GB18030') asc";
            case "py_desc":
                return "convert_to("+field+",'GB18030') desc";
            default:
                return field + " " + SORT_CONVERT.get(value);
        }

    }
}
