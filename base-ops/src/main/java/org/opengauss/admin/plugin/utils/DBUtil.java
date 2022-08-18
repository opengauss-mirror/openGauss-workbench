package org.opengauss.admin.plugin.utils;

import cn.hutool.crypto.SecureUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/10/10 11:14
 **/
@Slf4j
@Component
public class DBUtil {

    private static final ConcurrentHashMap<String, Connection> CONNECTION_CACHE = new ConcurrentHashMap<>();

    public Optional<Connection> getSession(String host, Integer port, String username, String password) throws SQLException, ClassNotFoundException {
        String driver = "org.opengauss.Driver";
        String sourceURL = "jdbc:opengauss://" + host + ":" + port + "/postgres";
        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(sourceURL, info);

        return Optional.ofNullable(conn);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ConnectionInfoSummary {
        private String host;
        private Integer port;
        private String username;
        private String password;

        public String summary() {
            return SecureUtil.md5(this.host + "_" + this.port + "_" + this.username + "_" + this.password);
        }
    }
}
