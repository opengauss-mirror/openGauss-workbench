/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * BasicEntity
 *
 * @author liu
 * @since 2022-10-01
 */
public class BasicEntity {
    /**
     * searchContent
     */
    private String searchContent;

    /**
     * createBy
     */
    private String createBy;

    /**
     * createTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;

    /**
     * updateBy
     */
    private String updateBy;

    /**
     * updateTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date updateTime;

    /**
     * description
     */
    private String description;

    /**
     * paramMap
     */
    private Map<String, Object> paramMap;

    public String getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParamMap() {
        if (null == paramMap) {
            paramMap = new HashMap<>();
        }
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }
}
