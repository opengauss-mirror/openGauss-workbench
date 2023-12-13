/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.dto.WinInfoDTO;
import com.nctigba.datastudio.service.impl.sql.TableDataServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * ResultSetMapDAO
 *
 * @since 2023-06-26
 */
@Slf4j
@Repository
@EnableScheduling
public class ResultSetMapDAO {
    /**
     * win map
     */
    public static Map<String, WinInfoDTO> winMap = new HashMap<>();

    @Autowired
    @Lazy
    private TableDataServiceImpl tableDataServiceImpl;


    /**
     * over time
     *
     */
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
                overtimeCloseWin(key);
            }
        }
    }

    /**
     * over time close win
     *
     * @param winID winID
     */
    public void overtimeCloseWin(String winID) {
        log.info("socketSet is: " + winID);
        winMap.remove(winID);
    }
}
