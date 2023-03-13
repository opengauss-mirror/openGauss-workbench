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
import java.util.*;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Repository
@EnableScheduling
public class ConnectionMapDAO {
    public static Map<String, ConnectionDTO> conMap = new HashMap<>(1);
    public static void setConMap(String uuiD, ConnectionDTO con) throws Exception {
        if (conMap.size() < 100) {
            conMap.put(uuiD, con);
        } else {
            log.info("The number of connections reached 100, conMap is: " + conMap);
            throw new Exception(LocaleString.transLanguage("2014"));
        }

    }

    @Scheduled(fixedRate=2 ,timeUnit = HOURS)
    public void overtime() {
        Date nowData = new Date();
        for (String key : conMap.keySet()) {
            ConnectionDTO connectionDTO = conMap.get(key);
            Date lastDate = connectionDTO.getLastDate();
            long diff = nowData.getTime() - lastDate.getTime();
            log.info("diff is: " + diff);
            if (diff > 2 * 60 * 60 * 1000) {
                try {
                    overtimeCloseSocket(connectionDTO.getSocketSet());
                    conMap.remove(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void deleteConnection(String uuid) {
        if(!conMap.containsKey(uuid)){
            throw new CustomException(LocaleString.transLanguage("1004"));
        }
        try {
            ConnectionDTO connectionDTO= conMap.get(uuid);
            log.info("connectionDTO is: " + connectionDTO);
            overtimeCloseSocket(connectionDTO.getSocketSet());
            log.info("old conMap is: " + conMap);
            conMap.remove(uuid);
            log.info("new conMap is: " + conMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Autowired
    WebSocketServer webSocketServer;

    public void overtimeCloseSocket(HashSet socketSet) throws Exception {
        log.info("socketSet is: " + socketSet);
        Iterator<String> iterator = socketSet.iterator();
        log.info("iterator is: " + iterator);
        while (iterator.hasNext()) {
            String winNmae = iterator.next();
            Statement statement = webSocketServer.getStatement(winNmae);
            Connection connection = webSocketServer.getConnection(winNmae);
            if (statement != null) {
                statement.close();
                statement.cancel();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

}
