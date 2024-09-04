/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ConnectionMapDAO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/dao/ConnectionMapDAO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.dao;


import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MINUTES;

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
    public static final Map<String, ConnectionDTO> conMap = new ConcurrentHashMap<>(1);

    @Autowired
    WebSocketServer webSocketServer;

    public static void setConMap(String uuiD, ConnectionDTO con) {
        if (conMap.size() <= 100) {
            conMap.put(uuiD, con);
        } else {
            log.info("The number of connections reached 100, conMap is: " + conMap);
            throw new CustomException(LocaleStringUtils.transLanguage("2014"));
        }
    }

    /**
     * over time
     *
     * @throws SQLException SQLException
     */
    @Scheduled(fixedRate = 2, timeUnit = MINUTES)
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

            }
            if (diff > connectionDTO.getTimeLength() * 60 * 60 * 1000) {
                log.info("connectionDTO.getSocketSet() is: " + connectionDTO.getSocketSet());
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
            webSocketServer.onClose("webds-plugin", winName);
        }
    }

}
