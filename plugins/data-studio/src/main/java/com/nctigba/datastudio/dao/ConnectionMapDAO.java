package com.nctigba.datastudio.dao;


import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
public class ConnectionMapDAO {
    public static Map<String, ConnectionDTO> conMap = new HashMap<>(1);

    public static void setConMap(String uuiD, ConnectionDTO con) throws Exception {
        if (conMap.size() < 100) {
            conMap.put(uuiD, con);
        } else {
            throw new Exception("The number of connections reached 100, unable to connect");
        }

    }

    public void overtime() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Date nowData = new Date();
                for (String key : conMap.keySet()) {
                    ConnectionDTO connectionDTO = conMap.get(key);
                    Date lastDate = connectionDTO.getLastDate();
                    long diff = nowData.getTime() - lastDate.getTime();
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
        };

        Timer timer = new Timer();

        timer.schedule(timerTask, 1000, 2 * 60 * 60 * 1000);
    }

    public void deleteConnection(String uuid) {
        try {
            log.info("wyl is: " + uuid);
            ConnectionDTO connectionDTO = conMap.get(uuid);
            log.info("wyl is: " + connectionDTO);
            overtimeCloseSocket(connectionDTO.getSocketSet());
            log.info("socketSet is: " + conMap);
            conMap.remove(uuid);
            log.info("socketSet is: " + conMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public static void main(String[] args) {
        ConnectionDTO con = new ConnectionDTO();
        conMap.put("1", con);
        conMap.put("2", con);
        conMap.put("3", con);
        conMap.put("4", con);
        conMap.put("5", con);
        HashSet<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.add("4");
        set.add("5");
        //创建一个迭代器
        Iterator iterator = set.iterator();
        //输出迭代之后的值
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
