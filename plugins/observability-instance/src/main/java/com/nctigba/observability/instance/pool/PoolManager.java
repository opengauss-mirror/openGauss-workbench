package com.nctigba.observability.instance.pool;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import com.nctigba.observability.instance.factory.InstanceHandlerFactory;
import com.nctigba.observability.instance.handler.instance.InstanceHandler;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoolManager {
    private final InstanceHandlerFactory instanceHandlerFactory;
    private static final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();


    /**
     * delete dataSource
     * 
     * @param nodeInfo node info
     */
    public void removeDataSource(InstanceNodeInfo nodeInfo) {
        String key = getKey(nodeInfo);
        if (!(dataSourceMap.containsKey(key))) {
            return;
        }
        dataSourceMap.remove(key);
    }

    private String getKey(InstanceNodeInfo nodeInfo) {
        return nodeInfo.getIp() + "_" + nodeInfo.getPort() + "_" + nodeInfo.getDbName() + "_" + nodeInfo.getDbUser();
    }


    private void addOrUpdateDataSource(InstanceNodeInfo nodeInfo, boolean updateFlag) {
        InstanceHandler instanceHandler = instanceHandlerFactory.getInstance(nodeInfo.getDbType());
        String key = getKey(nodeInfo);
        DataSource dataSource = dataSourceMap.get(key);
        String querySql = "select 1";
        if (ObjectUtils.isEmpty(dataSource)) {
            synchronized (PoolManager.class) {
                dataSource = instanceHandler.getDataSource(nodeInfo);
                if (!(instanceHandler.testConnectStatus(dataSource, querySql))) {
                    removeDataSource(nodeInfo);
                    return;
                }
                dataSourceMap.put(key, dataSource);
            }
        } else if (updateFlag && !(instanceHandler.testConnectStatus(dataSource, querySql))) {
            removeDataSource(nodeInfo);
        }
    }

    /**
     * get dataSource
     * 
     * @param nodeInfo node info
     * @return DataSource Data source information
     */
    public DataSource getDataSource(InstanceNodeInfo nodeInfo) {
        String key = getKey(nodeInfo);
        DataSource dataSource = dataSourceMap.get(key);
        if (ObjectUtils.isNotEmpty(dataSource)) {
            return dataSource;
        }
        addOrUpdateDataSource(nodeInfo, false);
        return dataSourceMap.get(key);
    }

    public void close(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                log.error("close resource fail:{}", e.getMessage());
            }
        }
    }
}
