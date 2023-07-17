/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;


import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * ConnectionMapDAO
 *
 * @since 2023-6-26
 */
@Slf4j
@Repository
@EnableScheduling
public class ConnectionMapDAO {
    /**
     * connection map
     */
    public static Map<String, ConnectionDTO> conMap = new HashMap<>(1);
    @Autowired
    WebSocketServer webSocketServer;

    public static void setConMap(String uuiD, ConnectionDTO con) {
        if (conMap.size() <= 100) {
            conMap.put(uuiD, con);
        } else {
            log.info("The number of connections reached 100, conMap is: " + conMap);
            throw new CustomException(LocaleString.transLanguage("2014"));
        }

    }

    /**
     * over time
     *
     * @throws SQLException SQLException
     */
    @Scheduled(fixedRate = 2, timeUnit = HOURS)
    public void overtime() throws SQLException {
        log.info("Start scheduled cleanup of connections. Connection number is: " + conMap.size());
        Date nowData = new Date();
        log.info("conMap is: {}", conMap);
        for (String key : conMap.keySet()) {
            ConnectionDTO connectionDTO = conMap.get(key);
            log.info("connectionDTO is: " + connectionDTO);
            Date lastDate = connectionDTO.getLastDate();
            long diff = nowData.getTime() - lastDate.getTime();
            log.info("diff is: " + diff);
            if (diff > 2 * 60 * 60 * 1000) {
                    log.info("connectionDTO.getSocketSet() is: " + connectionDTO.getSocketSet());
                    overtimeCloseSocket(connectionDTO.getSocketSet());
                    conMap.remove(key);

            }
            log.info("End scheduled cleanup of connections. Connection number is: " + conMap.size());
        }
    }

    /**
     * delete connection
     *
     * @param uuid uuid
     * @throws SQLException SQLException
     */
    public void deleteConnection(String uuid) throws SQLException {
        if (conMap.containsKey(uuid)) {
                ConnectionDTO connectionDTO = conMap.get(uuid);
                log.info("connectionDTO is: " + connectionDTO);
                overtimeCloseSocket(connectionDTO.getSocketSet());
                log.info("old conMap is: " + conMap);
                conMap.remove(uuid);
                log.info("new conMap is: " + conMap);
        }
    }

    public void overtimeCloseSocket(HashSet<String> socketSet) throws SQLException {
        log.info("socketSet is: " + socketSet);
        Iterator<String> iterator = socketSet.iterator();
        log.info("iterator is: " + iterator);
        while (iterator.hasNext()) {
            String winName = iterator.next();
            Statement statement = webSocketServer.getStatement(winName);
            Connection connection = webSocketServer.getConnection(winName);
            if (statement != null) {
                statement.cancel();
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

}
