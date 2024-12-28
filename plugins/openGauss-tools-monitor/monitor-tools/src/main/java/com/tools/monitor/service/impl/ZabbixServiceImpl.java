/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.tools.monitor.entity.DataSource;
import com.tools.monitor.entity.zabbix.WholeIds;
import com.tools.monitor.entity.zabbix.ZabbbixMap;
import com.tools.monitor.entity.zabbix.ZabbixMessge;
import com.tools.monitor.entity.zabbix.ZabbixSql;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.util.HandleUtils;
import com.tools.monitor.util.StringUtils;
import com.tools.monitor.util.jdbc.JdbcUtil;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * ZabbixServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Service
public class ZabbixServiceImpl {
    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    private static final String LINE_FEED = String.valueOf(StrUtil.C_LF);

    private static final String INSERT_HOST = "insert into hosts(hostid,host,status,available,name,description) "
            + "values(%s,'%s',0,1,'%s','');";

    private static final String INSERT_INTERFACE = "insert into interface values (%s, %s, 1, 1, 1, '%s', '', '%s');";

    private static final String METRIC_NAME = "Prometheus_node_metric"; // 指标名称是否合适？

    private static final String INSERT_PROC = "insert into item_preproc values(%s,%s,1,22,'%s',0,'');";

    private static final String INSERT_RTDATA = "insert into item_rtdata values(%s,0,0,0,'');";

    private static final String INSERT_ITEM = "insert into items(itemid,type,hostid,name,key_,delay,value_type,"
            + "params,interfaceid,description,posts,headers,master_itemid,timeout,url) "
            + "values(%s,%s,%s,'%s','%s','%s',%s,'',%s,'','','',%s,'%s','%s');";

    private static final String HOSTID_BY_HOST_NAME = "select hostid from hosts where host = '%s';";

    private static final String ITEMID_BY_MASTER_HOSTID = "select itemid from items where hostid = %S "
            + "and name = 'Prometheus_node_metric';";

    private static final String SELECT_ITEM_BY_NAME = "select itemid from items where name = '%s';";

    private static final String SELECT_ALL_ID = "select field_name,nextid from ids";

    private static final String UPDATE_ID_HOST = "update ids set nextid = %S  where field_name = 'hostid';";

    private static final String UPDATE_ID_INTERFACE = "update ids set nextid = %S  where field_name = 'interfaceid';";

    private static final String UPDATE_ID_ITEM_PREPROC = "update ids set nextid = %S  "
            + "where field_name = 'item_preprocid';";

    private static final String UPDATE_ID_ITEM = "update ids set nextid = %S  where field_name = 'itemid';";

    @Autowired
    SysJobServiceImpl sysJobServiceImpl;

    @Autowired
    private CommonServiceImpl commonService;

    /**
     * insertNecessary
     *
     * @param name name
     * @param ip   ip
     * @param port port
     * @return ZabbixMessge
     */
    public ZabbixMessge insertNecessary(String name, String ip, String port) {
        List<String> hostAndInterface = new ArrayList<>();
        hostAndInterface.add("SET FOREIGN_KEY_CHECKS = 0;");
        DataSource dataSource = commonService.getDataSource();
        String hostIdByName = String.format(Locale.ROOT, HOSTID_BY_HOST_NAME, name);
        List<JSONObject> json = JdbcUtil.executeSql(dataSource, hostIdByName);
        if (CollectionUtil.isNotEmpty(json)) {
            Integer hostId = JdbcUtil.getResulte("hostid", json);
            String itemSql = String.format(Locale.ROOT, ITEMID_BY_MASTER_HOSTID, hostId);
            List<JSONObject> fatherIdem = JdbcUtil.executeSql(dataSource, itemSql);
            Integer fatherItemId = JdbcUtil.getResulte("itemid", fatherIdem);
            return new ZabbixMessge(hostId, fatherItemId, dataSource, name);
        }
        WholeIds wholeIds = JdbcUtil.getAllId(JdbcUtil.executeSql(dataSource, SELECT_ALL_ID));
        Integer hostid = wholeIds.getHostid() + 1;
        Integer interfaceid = wholeIds.getInterfaceid() + 1;
        Integer itemid = wholeIds.getItemid() + 1;
        // host
        String hostSql = String.format(Locale.ROOT, INSERT_HOST, hostid, name, name);
        hostAndInterface.add(hostSql);
        // interface
        String interFaceSql = String.format(Locale.ROOT, INSERT_INTERFACE, interfaceid, hostid, ip, port);
        hostAndInterface.add(interFaceSql);
        // item
        String url = "http://" + IpUtils.formatIp(ip) + ":" + port + "/prometheus";
        String itemSql = String.format(Locale.ROOT, INSERT_ITEM, itemid, 19, hostid,
                METRIC_NAME, METRIC_NAME, "10s", 4, interfaceid, null, "30m", url);
        String updateItemSql = String.format(Locale.ROOT, UPDATE_ID_ITEM, itemid);
        hostAndInterface.add(itemSql);
        hostAndInterface.add(updateItemSql);
        // item_rtdata
        String reqDataSql = String.format(Locale.ROOT, INSERT_RTDATA, itemid);
        String updateHostSqL = String.format(Locale.ROOT, UPDATE_ID_HOST, hostid);
        String updateFaceSql = String.format(Locale.ROOT, UPDATE_ID_INTERFACE, interfaceid);
        hostAndInterface.add(reqDataSql);
        hostAndInterface.add(updateHostSqL);
        hostAndInterface.add(updateFaceSql);
        hostAndInterface.add(updateItemSql);
        hostAndInterface.add("SET FOREIGN_KEY_CHECKS = 1;");
        hostAndInterface.add("commit;");
        // Execute sql
        JdbcUtil.batchExecuteSql(dataSource, hostAndInterface, name, name);
        return new ZabbixMessge(hostid, itemid, dataSource, name);
    }

    /**
     * insertTarget
     *
     * @param zabbixMessge zabbixMessge
     * @param zabbix       zabbix
     * @param jdbcTemplate jdbcTemplate
     */
    public void insertTarget(ZabbixMessge zabbixMessge, List<SysJob> zabbix, JdbcTemplate jdbcTemplate) {
        List<String> sql = zabbix.stream().map(SysJob::getInvokeTarget).collect(Collectors.toList());
        List<ZabbixSql> zabbixSqls = getSqlString(sql);
        String hoatName = zabbixMessge.getHostName();
        for (ZabbixSql zabbixSql : zabbixSqls) {
            List<ZabbbixMap> zabbbixMaps = getResulte(zabbixSql, jdbcTemplate, hoatName);
            if (CollectionUtil.isNotEmpty(zabbbixMaps)) {
                ZabbbixMap zabbbixMap = zabbbixMaps.get(0);
                String repeat = String.format(Locale.ROOT, SELECT_ITEM_BY_NAME, zabbbixMap.getZabbixTargetName());
                List<JSONObject> jsonObjects = JdbcUtil.executeSql(zabbixMessge.getDataSource(), repeat);
                if (CollectionUtil.isNotEmpty(jsonObjects)) {
                    continue;
                }
            }
            // Generate the corresponding insertSql according to total;
            insertOneTarget(zabbixMessge, zabbbixMaps, zabbixSql.getOnly(), hoatName);
        }
    }

    private void insertOneTarget(ZabbixMessge zabbixMessge, List<ZabbbixMap> zabbbixMaps, String only,
        String hoatName) {
        if (CollectionUtil.isEmpty(zabbbixMaps)) {
            return;
        }
        List<String> insertSql = getAllSql(zabbbixMaps, zabbixMessge);
        insertSql.add("SET FOREIGN_KEY_CHECKS = 1;");
        insertSql.add("commit;");
        if (CollectionUtil.isNotEmpty(insertSql)) {
            JdbcUtil.batchExecuteSql(zabbixMessge.getDataSource(), insertSql, only, hoatName);
        }
    }

    private List<String> getAllSql(List<ZabbbixMap> total, ZabbixMessge zabbixMessge) {
        List<String> finalSql = new ArrayList<>();
        finalSql.add("SET FOREIGN_KEY_CHECKS = 0;");
        if (CollectionUtil.isNotEmpty(total)) {
            WholeIds wholeIds = JdbcUtil.getAllId(JdbcUtil.executeSql(zabbixMessge.getDataSource(), SELECT_ALL_ID));
            Integer hostId = zabbixMessge.getHostId();
            Integer father = zabbixMessge.getFatherItemId();
            Integer nextItem = wholeIds.getItemid() + 1;
            Integer nextProc = wholeIds.getItemPreprocid() + 1;
            for (ZabbbixMap zabbbixMap : total) {
                String sonItemSql = String.format(Locale.ROOT, INSERT_ITEM, nextItem, 18, hostId,
                        zabbbixMap.getZabbixTargetName(), zabbbixMap.getZabbixKey(), "0", 0, null, father, "10s", "");
                String updateItemId = String.format(Locale.ROOT, UPDATE_ID_ITEM, nextItem);
                String itemRtdataSql = String.format(Locale.ROOT, INSERT_RTDATA, nextItem);
                String processSql = String.format(Locale.ROOT, INSERT_PROC, nextProc, nextItem,
                        zabbbixMap.getZabbixTargetProcess());
                String updateProcId = String.format(Locale.ROOT, UPDATE_ID_ITEM_PREPROC, nextProc);
                finalSql.add(sonItemSql);
                finalSql.add(updateItemId);
                finalSql.add(processSql);
                finalSql.add(updateProcId);
                finalSql.add(itemRtdataSql);
                nextItem = nextItem + 1;
                nextProc = nextProc + 1;
            }
        }
        return finalSql;
    }

    /**
     * getResulte
     *
     * @param zabbixSql    zabbixSql
     * @param jdbcTemplate jdbcTemplate
     * @param hostName     hostName
     * @return list
     */
    public List<ZabbbixMap> getResulte(ZabbixSql zabbixSql, JdbcTemplate jdbcTemplate, String hostName) {
        List<ZabbbixMap> mapList = new ArrayList<>();
        List<Map<String, Object>> list = null;
        try {
            list = commonService.executeSql(jdbcTemplate, zabbixSql.getSql());
        } catch (DataAccessException exception) {
            return new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(list)) {
            return mapList;
        }
        for (Map<String, Object> maps : list) {
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                if (ObjectUtil.isEmpty(entry.getValue())
                        && !entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "default");
                }
                if (ObjectUtil.isEmpty(entry.getValue())
                        && entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "0");
                }
                if (entry.getValue().toString().startsWith(".")) {
                    String value = "0" + entry.getValue().toString();
                    maps.put(entry.getKey(), value);
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> arry = list.get(i);
            String end = HandleUtils.getZabbixMap(HandleUtils.getMap(arry), i);
            for (Map.Entry<String, Object> entry : arry.entrySet()) {
                if (entry.getValue().toString().matches(ISNUM) || entry.getValue().toString().matches(KEXUE)) {
                    StringBuilder zabbixKey = new StringBuilder();
                    zabbixKey.append(entry.getKey()).append("_").append(zabbixSql.getOnly()).append(i);
                    StringBuilder zabbixTargetName = new StringBuilder();
                    StringBuilder zabbixProcess = new StringBuilder();
                    zabbixTargetName.append(entry.getKey())
                            .append("_").append(zabbixSql.getOnly()).append("_").append(hostName);
                    zabbixProcess.append(zabbixTargetName).append(end).append(LINE_FEED);
                    ZabbbixMap zabbbixMap = new ZabbbixMap(zabbixTargetName.toString(),
                            zabbixKey.toString(), zabbixProcess.toString());
                    mapList.add(zabbbixMap);
                }
            }
        }
        return mapList;
    }

    private List<ZabbixSql> getSqlString(List<String> list) {
        List<ZabbixSql> zabbixSqls = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (String str : list) {
                String methodStr = StringUtils.substringBetween(str, "(", "$");
                String[] methodParams = methodStr.split("#");
                ZabbixSql zabbixSql = new ZabbixSql();
                String sql = methodParams[0];
                String only = methodParams[1];
                zabbixSql.setSql(StringUtils.substring(sql, 1, sql.length() - 1));
                zabbixSql.setOnly(StringUtils.substring(only, 1, only.length() - 1));
                zabbixSqls.add(zabbixSql);
            }
        }
        return zabbixSqls;
    }
}