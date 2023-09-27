/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.config;

import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.ParamInfo;
import com.nctigba.observability.sql.model.param.ParamInfoTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ParamInfoInitConfig {
    @Value("${sqlitePath:data/" + CommonConstants.PARAM_DATABASE_NAME + ".db}")
    private String path;
    @Value("${sqliteinit:false}")
    private boolean refresh;
    boolean needInit = false;

    @PostConstruct
    public void init() throws IOException {
        List<String> initSqls = new ArrayList<>();
        ParamInfoTable paramInfoTable = new ParamInfoTable();
        initSqls.add(paramInfoTable.getTableDefine());
        List<ParamInfo> paramInfos = ParamInfo.getStaticInfos();
        for (ParamInfo paramInfo : paramInfos) {
            String insertStatement = paramInfoTable.insert(paramInfo);
            initSqls.add(insertStatement);
        }
        File f = new File(path);
        log.info("sqlite:" + f.getCanonicalPath());
        if (!f.exists()) {
            needInit = true;
            var parent = f.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            boolean b = f.createNewFile();
            if (!b) {
                log.error("ParamInfoInitConfig init createNewFile fail");
            }
        } else if (refresh) {
            needInit = true;
            Files.delete(f.toPath());
            boolean b = f.createNewFile();
            if (!b) {
                log.error("ParamInfoInitConfig init createNewFile fail");
            }
        }
        if (needInit) {
            var sqLiteDataSource = new SQLiteDataSource();
            sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath());
            try (var conn = sqLiteDataSource.getConnection();) {
                for (String sql : initSqls) {
                    try {
                        conn.createStatement().execute(sql);
                    } catch (SQLException e) {
                        log.info(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
