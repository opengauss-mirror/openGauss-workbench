/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.global;

import lombok.Data;

/**
 * The AppConfig class is used to store the configuration information of the application.
 *
 * @since 2024/11/4
 */
@Data
public class AppConfig {
    private Datakit datakit;

    private OpsHost opsHost;

    private Database database;

    /**
     * datakit configurations
     */
    @Data
    public static class Datakit {
        private String username;
        private String password;
        private String serverUrl;
        private String pluginPath;
    }

    /**
     * opsHost configurations
     */
    @Data
    public static class OpsHost {
        private String privateIp;
        private String publicIp;
        private int port;
        private String password;
        private User user;

        /**
         * ops host command user information
         */
        @Data
        public static class User {
            private String name;
            private String password;
        }
    }

    /**
     * database configurations
     */
    @Data
    public static class Database {
        private Jdbc jdbc;

        /**
         * jdbc database configurations
         */
        @Data
        public static class Jdbc {
            private MySql mysql;
            private OpenGauss opengauss;

            /**
             * jdbc MySql database information
             */
            @Data
            public static class MySql {
                private String hostIp;
                private int port;
                private String username;
                private String password;
            }

            /**
             * jdbc openGauss database information
             */
            @Data
            public static class OpenGauss {
                private String hostIp;
                private int port;
                private String username;
                private String password;
            }
        }
    }
}

