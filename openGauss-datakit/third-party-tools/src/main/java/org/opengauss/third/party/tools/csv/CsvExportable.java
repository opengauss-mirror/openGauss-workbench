/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.csv;

/**
 * Csv exportable interface
 *
 * @since 2026/6/8
 */
public interface CsvExportable {
    /**
     * Get csv header
     *
     * @return csv header
     */
    String getCsvHeader();

    /**
     * Get csv row data
     *
     * @return csv row data
     */
    String getCsvData();
}
