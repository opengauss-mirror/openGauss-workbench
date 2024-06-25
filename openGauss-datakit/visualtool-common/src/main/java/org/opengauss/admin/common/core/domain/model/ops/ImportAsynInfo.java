package org.opengauss.admin.common.core.domain.model.ops;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Progress Information Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
@Data
public class ImportAsynInfo {
    private String msg;
    private Integer totality = 0;
    private Integer doneSum = 0;
    private Integer errorSum = 0;
    private Integer successSum = 0;
    public Boolean isEnd = false;

    public Boolean getEnd() {
        return isEnd;
    }

    public void setEnd(Boolean end) {
        isEnd = end;
    }
}