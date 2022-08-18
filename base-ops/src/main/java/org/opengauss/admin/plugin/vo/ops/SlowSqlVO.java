package org.opengauss.admin.plugin.vo.ops;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/12 18:11
 **/
@Data
public class SlowSqlVO {
    private String start_time;
    private String finish_time;
    private String slow_sql_threshold;
    private String query;
    private String query_plan;
}
