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
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static java.util.concurrent.TimeUnit.HOURS;

@Slf4j
@Repository
@EnableScheduling
public class ConnectionMapDAO {
    public static Map<String, ConnectionDTO> conMap = new HashMap<>(1);

    public static void setConMap(String uuiD, ConnectionDTO con) throws Exception {
        if (conMap.size() <= 100) {
            conMap.put(uuiD, con);
        } else {
            log.info("The number of connections reached 100, conMap is: " + conMap);
            throw new Exception(LocaleString.transLanguage("2014"));
        }

    }

    @Scheduled(fixedRate = 2, timeUnit = HOURS)
    public void overtime() {
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

                try {
                    log.info("connectionDTO.getSocketSet() is: " + connectionDTO.getSocketSet());
                    overtimeCloseSocket(connectionDTO.getSocketSet());
                    conMap.remove(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("End scheduled cleanup of connections. Connection number is: " + conMap.size());
        }
    }

    public void deleteConnection(String uuid) {
        if (conMap.containsKey(uuid)) {
            try {
                ConnectionDTO connectionDTO = conMap.get(uuid);
                log.info("connectionDTO is: " + connectionDTO);
                overtimeCloseSocket(connectionDTO.getSocketSet());
                log.info("old conMap is: " + conMap);
                conMap.remove(uuid);
                log.info("new conMap is: " + conMap);
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        }
    }

    @Autowired
    WebSocketServer webSocketServer;

    public void overtimeCloseSocket(HashSet socketSet) throws Exception {
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
