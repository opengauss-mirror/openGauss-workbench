/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengauss.admin.plugin.constants.TaskAlertConstants;
import org.opengauss.admin.plugin.enums.AlertSourceEnum;

import java.util.Locale;

/**
 * tb migration task alert
 *
 * @since 2024/12/16
 */
@Data
@TableName("tb_migration_task_alert")
public class MigrationTaskAlert {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private int taskId;
    private int migrationPhase;
    private String dateTime;
    private String thread;
    private String className;
    private String methodName;
    private int lineNumber;
    private String causeCn;
    private String causeEn;
    private String logLevel;
    private String logCode;
    private int logSource;
    private String detail;

    @TableField(exist = false)
    private String message;

    /**
     * generate alert detail
     */
    public void generateAlertDetail() {
        String toolBySourceId = AlertSourceEnum.getToolBySourceId(this.logSource);
        if (TaskAlertConstants.MigrationTools.PORTAL.equals(toolBySourceId)) {
            detail = String.format("%s [%s] %s %s - <CODE:%s> %s",
                    dateTime, thread, logLevel, className, logCode, message);
        } else if (TaskAlertConstants.MigrationTools.CHAMELEON.equals(toolBySourceId)) {
            detail = String.format("%s %s %s %s <CODE:%s> %s",
                    dateTime, thread, logLevel, className, logCode, message);
        } else if (TaskAlertConstants.MigrationTools.DATA_CHECKER.equals(toolBySourceId)) {
            detail = String.format("%s [%s] %s [%s] - <CODE:%s> %s",
                    dateTime, thread, logLevel, className, logCode, message);
        } else {
            detail = String.format(Locale.ROOT, "[%s] %s <CODE:%s> %s (%s:%d)",
                    dateTime, logLevel, logCode, message, className, lineNumber);
        }
    }
}
