/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * TableDataInfo.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/page/TableDataInfo.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 * table data bean
 *
 * @author xielibo
 */
public class TableDataInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * total
     */
    private long total;

    /**
     * rows
     */
    private List<?> rows;

    /**
     * code
     */
    private int code;

    /**
     * msg
     */
    private String msg;

    /**
     * table data
     */
    public TableDataInfo() {
    }

    public TableDataInfo(List<?> list, int total) {
        this.rows = list;
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
