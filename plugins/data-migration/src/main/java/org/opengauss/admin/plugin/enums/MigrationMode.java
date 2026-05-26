package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Migration task mode
 *
 * @since 2026/05/25
 */
@AllArgsConstructor
@Getter
public enum MigrationMode {
    OFFLINE(1),
    ONLINE(2),
    OFFLINE_WITHOUT_DATA_CHECK(3),
    ONLINE_WITHOUT_DATA_CHECK(4);
    private Integer code;

    /**
     * Is mode has incremental and reverse migration
     *
     * @param migrationModelId migration model id
     * @return true if has incremental and reverse migration, false otherwise
     */
    public static boolean hasIncrementalAndReverse(Integer migrationModelId) {
        return ONLINE.getCode().equals(migrationModelId)
                || ONLINE_WITHOUT_DATA_CHECK.getCode().equals(migrationModelId);
    }
}
