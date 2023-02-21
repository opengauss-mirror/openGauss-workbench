package com.nctigba.datastudio.dao;


import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


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
                        } catch (SQLException e) {
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
            ConnectionDTO connectionDTO = conMap.get(uuid);
            overtimeCloseSocket(connectionDTO.getSocketSet());
            conMap.remove(uuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    WebSocketServer webSocketServer;

    public void overtimeCloseSocket(HashSet socketSet) throws SQLException {
        Iterator iterator = socketSet.iterator();
        while (iterator.hasNext()) {
            Statement statement = webSocketServer.getStatement((String) iterator.next());
            if (statement != null) {
                statement.close();
                statement.cancel();
            }
            Connection connection = webSocketServer.getConnection((String) iterator.next());
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
