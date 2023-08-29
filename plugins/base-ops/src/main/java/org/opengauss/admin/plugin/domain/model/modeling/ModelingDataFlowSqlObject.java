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
package org.opengauss.admin.plugin.domain.model.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@Data
public class ModelingDataFlowSqlObject implements Serializable {
    private String mainType;

    private String mainOperatorId;

    private String preparedSql;

    private String executeDbName;

    private String executeClusterId;

    private String executeClusterNodeId;

    private String executeSchema;

    private OpsClusterNodeVO executeClusterNode;

    public List<String> preparedParams = new ArrayList<>();

    public String addParam(String param) {
        int index = preparedParams.size();
        preparedParams.add(param);
        return "#" + index + "#";
    }

    public String addParamList(List<String> paramList) {
        StringBuilder sb = new StringBuilder();
        for (String param : paramList) {
            sb.append(addParam(param)).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String finalSql;

    private String selectRegion = "select ";

    private String updateRegion = "update ";

    private String deleteRegion = "delete from ";

    private String insertRegion = "insert into ";

    private String insertFieldsRegion = " values ";

    private String updateFieldsRegion = "set ";

    private String fromRegion = "";

    private String joinRegion = "";

    private String whereRegion = "";

    private String groupRegion = "";

    private String orderByRegion = "";

    private String limitRegion = "";

    private String polymerizationRegion = "";

    private  List<String> mappingRegion = new ArrayList<>();


    public ModelingDataFlowSqlObject initDataBase(OpsClusterNodeVO node, String defaultSchema) {
        if (this.getExecuteSchema() == null) {
            this.setExecuteSchema(defaultSchema);
        }
        if (node == null) {
            throw new RuntimeException("cluster node info not found!");
        }
        this.setExecuteClusterNode(node);

        return this;
    }

    public String toSql() {
        return getFinalSql();
    }

    public String getSqlWithType() {
        String sql;
        switch (this.getMainType()) {
            case "query":
                sql = toQuerySql();
                break;
            case "update":
                sql = toUpdateSql();
                break;
            case "delete":
                sql = toDeleteSql();
                break;
            case "insert":
                sql = toInsertSql();
                break;
            default:
                sql = "";
        }
        return sql;
    }

    public String toQuerySql()
    {
        sqlObjectPretreatment();
        return  (this.getSelectRegion() + this.getFromRegion() + this.getJoinRegion() + this.getWhereRegion() + this.getGroupRegion() + this.getOrderByRegion() + this.getLimitRegion()).trim()+";";
    }

    public String toDeleteSql()
    {
        return ( this.getDeleteRegion() + this.getWhereRegion() + this.getLimitRegion() ).trim()+";";
    }

    public String toInsertSql()
    {
        return ( this.getInsertRegion() + this.getInsertFieldsRegion() ).trim()+";";
    }

    public String toUpdateSql()
    {
        return ( this.getUpdateRegion() + this.getUpdateFieldsRegion() + this.getWhereRegion() + this.getOrderByRegion() + this.getLimitRegion()).trim()+";";
    }

    /**
     * sql pretreatment
     */
    private void sqlObjectPretreatment() {
        groupRegionHandle();
        polymerizationRegionHandle();
        mappingRegionHandle();
        joinRegionHandle();
        limitRegionHandle();
    }

    /**
     * modify select region for group
     */
    private void groupRegionHandle() {
        if (this.getGroupRegion().length() > 0) {
            this.setSelectRegion("select "+this.getGroupRegion().substring(9));
        }
    }

    /**
     * modify select region for polymerization
     */
    private void polymerizationRegionHandle() {
        if (this.getPolymerizationRegion().length()>0) {
            if (this.getGroupRegion().length() > 0) {
                this.setSelectRegion(this.getSelectRegion().trim()+","+this.getPolymerizationRegion());
            } else {
                this.setSelectRegion("select "+this.getPolymerizationRegion());
            }
        }
    }

    /**
     * if exist join region then modify select [If there are fields with the same name +(1/2/3...)]
     */
    private void joinRegionHandle() {
        if (this.getJoinRegion().length() > 0) {
            String fieldsOldString = this.getSelectRegion().substring(7).trim();

            String[] fieldsOldArr = fieldsOldString.split(",(?![^()]*\\))");
            if (fieldsOldArr.length < 2) {
                return;
            }
            List<String> fieldsNewList = new ArrayList<>();
            Map<String, Integer> fieldsOldAllList = new HashMap<>();
            int fieldRepeat = 0;
            for (String fieldOld : fieldsOldArr) {
                String[] tableField = fieldOld.split("\\.");
                if (tableField.length == 2) {
                    String tableName = tableField[0];
                    String field = tableField[1];
                    if (fieldsOldAllList.get(field) == null) {
                        fieldsOldAllList.put(field, 0);
                        fieldsNewList.add(fieldOld);
                    } else {
                        fieldRepeat = 1;
                        int repeatTimes = fieldsOldAllList.get(field) + 1;
                        fieldsOldAllList.put(field, repeatTimes);
                        fieldsNewList.add(tableName + "." + field + " as " + field + "_" + repeatTimes);
                    }
                }
            }
            if (fieldRepeat == 1) {
                // if repeat
                String newFieldSql = String.join(",", fieldsNewList);
                this.setSelectRegion("select " + newFieldSql + " ");
            }
        }
    }


    /**
     * default limit 1000
     */
    private void limitRegionHandle() {
        if (this.getLimitRegion().length()<1) {
            this.setLimitRegion(" limit 1000");
        }
    }

    /**
     * if mapping region exist then modify select region
     */
    private void mappingRegionHandle() {
        List<String> mappingRegionTemp = this.getMappingRegion();
        if (mappingRegionTemp.size()>0) {
            //if mapping region exist
            String fieldsOldString = this.getSelectRegion().substring(7).trim();

            String fieldAll = "*";
            if (fieldsOldString.equals(fieldAll)) {
                this.setSelectRegion(this.getSelectRegion().trim()+","+String.join(",",this.getMappingRegion())+" ");
            } else {
                String[] fieldsOldArr = fieldsOldString.split(",");
                List<String> fieldsOldList = Arrays.asList(fieldsOldArr);
                List<String> fieldsNewList = new ArrayList<>(fieldsOldList);

                for (String mappingItem : mappingRegionTemp) {
                    String mappingField = mappingItem.split(" ")[0];
                    int indexSame = fieldsNewList.indexOf(mappingField);
                    if (indexSame != -1) {
                        // replace if include
                        fieldsNewList.set(indexSame, mappingItem);
                    } else {
                        // add if not include
                        fieldsNewList.add(mappingItem);
                    }
                }
                this.setSelectRegion("select "+String.join(",",fieldsNewList)+" ");
            }
        }
    }

    public ModelingDataFlowSqlObject doPrepare() {
        List<String> sortParamsList = new ArrayList<>(preparedParams.size());
        String oriSql = this.getSqlWithType();
        //find #index# sort params
        List<String> result = regStrip("#\\d+#",oriSql);
        result.forEach(index -> {
            sortParamsList.add(preparedParams.get(Integer.parseInt(index)));
        });
        preparedParams = sortParamsList;
        //replace format #index# with ?
        preparedSql = oriSql.replaceAll("#\\d+#","?");

        finalSql = getRealSql(preparedSql,preparedParams);
        return this;
    }

    public static List<String> regStrip(String patten,String textArea) {
        Pattern compile = Pattern.compile(patten);
        Matcher matcher = compile.matcher(textArea);
        List<String> targetList = new ArrayList<String>();
        while (matcher.find()) {
            String substring = textArea.substring(matcher.start()+1, matcher.end()-1);
            targetList.add(substring);
        }
        return targetList;
    }

    public static String getRealSql(String sql, List<String> params) {

        if(params == null || params.size() == 0) {
            return sql;
        }

        Pattern pattern = Pattern.compile("\\?");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(sb, "'" + params.get(index++) + "'");
        }
        matcher.appendTail(sb);

        return sb.toString();

    }
}
