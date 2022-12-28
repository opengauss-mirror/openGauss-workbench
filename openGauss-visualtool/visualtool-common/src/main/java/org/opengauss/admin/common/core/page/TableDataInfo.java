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
