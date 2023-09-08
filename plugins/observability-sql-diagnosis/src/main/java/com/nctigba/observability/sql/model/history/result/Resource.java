/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.result;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Resource
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@TableName(value = "diagnosis_resource", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class Resource {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer taskid;
    @TableField("grabType")
    GrabType grabType;
    byte[] f;

    /**
     * Construction method
     *
     * @param task Diagnosis task info
     * @param grabType Bcc type
     */
    public Resource(HisDiagnosisTask task, GrabType grabType) {
        super();
        this.taskid = task.getId();
        this.grabType = grabType;
    }

    /**
     * Copy method
     *
     * @param os read svg
     */
    public void to(OutputStream os) {
        try {
            IOUtils.copy(new ByteArrayInputStream(f), os);
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }
}