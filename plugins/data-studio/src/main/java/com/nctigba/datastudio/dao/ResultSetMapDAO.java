/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.dto.WinInfoDTO;
import com.nctigba.datastudio.service.impl.sql.TableDataServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Repository
@EnableScheduling
public class ResultSetMapDAO {
    public static Map<String, WinInfoDTO> winMap = new HashMap<>();

    public static void setWinMap(String winid, WinInfoDTO winInfoDTO) {
        winMap.put(winid, winInfoDTO);
        log.info("winMap is: " + winMap);
    }

    @Scheduled(fixedRate = 10, timeUnit = MINUTES)
    public void overtime() {
        Date nowData = new Date();
        Set<String> set = winMap.keySet();
        Date date;
        for (String key : set) {
            log.info("WinId is: " + key);
            date = winMap.get(key).getLastDate();
            long diff = nowData.getTime() - date.getTime();
            log.info("diff is: " + diff);
            if (diff > 10 * 60 * 1000) {
                try {
                    overtimeCloseWin(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Autowired
    TableDataServiceImpl tableDataServiceImpl;

    public void overtimeCloseWin(String winID) throws Exception {
        log.info("socketSet is: " + winID);
        log.info("tableDataServiceImpl.getConnectionMap is: {}" + tableDataServiceImpl.getConnectionMap(winID));
        log.info("tableDataServiceImpl.getSatementMap is: {}" + tableDataServiceImpl.getSatementMap(winID));
        log.info("tableDataServiceImpl.getResultSetMap is: {}" + tableDataServiceImpl.getResultSetMap(winID));
        Connection connection = tableDataServiceImpl.getConnectionMap(winID);
        Statement statement = tableDataServiceImpl.getSatementMap(winID);
        ResultSet resultSet = tableDataServiceImpl.getResultSetMap(winID);
        if (resultSet != null) {
            resultSet.close();
            tableDataServiceImpl.removeResultSetMap(winID);
        }
        if (statement != null) {
            statement.cancel();
            statement.close();
            tableDataServiceImpl.removeSatementMap(winID);
        }
        if (connection != null) {
            connection.close();
            tableDataServiceImpl.removeConnectionMap(winID);
        }
        winMap.remove(winID);
    }
}
