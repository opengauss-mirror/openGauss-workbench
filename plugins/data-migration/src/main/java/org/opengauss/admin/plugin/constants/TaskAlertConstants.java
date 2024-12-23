/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.constants;

/**
 * task alert constants
 *
 * @since 2024/12/16
 */
public interface TaskAlertConstants {
    /**
     * alert refresh intervals millisecond
     */
    Long ALERT_REFRESH_INTERVALS_MILLISECOND = 6000L;

    /**
     * cache expire time (minutes)
     */
    int CACHE_EXPIRE_TIME = 60;

    /**
     * alert file size
     */
    int ALERT_FILE_SIZE = 100;

    /**
     * alert log entity json string separator in alert file
     */
    String OBJECT_SEPARATOR = "<<<END_OF_OBJECT>>>";

    /**
     * alert file home path model
     */
    String ALERT_FILE_HOME_PATH_MODEL = "%s/workspace/%d/status/alert/";

    /**
     * alert file name model
     */
    String ALERT_FILE_NAME_MODEL = "alert_%d.txt";

    /**
     * parameters related to alert log configuration
     */
    interface Params {
        /**
         * configuration key to enable or disable alert log collection
         */
        String ENABLE_ALERT_LOG_COLLECTION = "enable.alert.log.collection";

        /**
         * configuration key for Kafka bootstrap servers
         */
        String KAFKA_SEVER = "kafka.bootstrapServers";
    }

    /**
     * migration tools name
     */
    interface MigrationTools {
        /**
         * migration tool portal
         */
        String PORTAL = "portal";

        /**
         * migration tool chameleon
         */
        String CHAMELEON = "chameleon";

        /**
         * migration tool datachecker
         */
        String DATA_CHECKER = "datachecker";

        /**
         * migration tool debezium
         */
        String DEBEZIUM = "debezium";
    }
}
