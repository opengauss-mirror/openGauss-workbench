/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

/**
 * OperateStatusDO
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class OperateStatusDO {
    @JsonProperty("debug")
    private boolean isDebug;
    @JsonProperty("compile")
    private boolean isCompile;

    @JsonProperty("execute")
    private boolean isExecute;

    @JsonProperty("startDebug")
    private boolean isStartDebug;

    @JsonProperty("stopDebug")
    private boolean isStopDebug;

    @JsonProperty("continueStep")
    private boolean isContinueStep;

    @JsonProperty("singleStep")
    private boolean isSingleStep;

    @JsonProperty("startRun")
    private boolean isStartRun;

    @JsonProperty("stopRun")
    private boolean isStopRun;

    @JsonProperty("stepIn")
    private boolean isStepIn;

    @JsonProperty("stepOut")
    private boolean isStepOut;

    @JsonProperty("coverageRate")
    private boolean isCoverageRate;

    public OperateStatusDO() {
        init();
    }

    /**
     * init button status
     */
    public void init() {
        this.isCompile = true;
        this.isExecute = false;
        this.isStartDebug = false;
        this.isStopDebug = false;
        this.isContinueStep = false;
        this.isSingleStep = false;
        this.isStartRun = false;
        this.isStopRun = false;
        this.isStepIn = false;
        this.isStepOut = false;
        this.isCoverageRate = false;
    }

    /**
     * enable stop run
     */
    public void enableStopRun() {
        this.isStartRun = true;
        this.isStopRun = false;
    }

    /**
     * enable start debug
     */
    public void enableStartDebug() {
        this.isCompile = true;
        this.isExecute = true;
        this.isStartDebug = true;
        this.isStopDebug = false;
        this.isContinueStep = false;
        this.isSingleStep = false;
        this.isStepIn = false;
        this.isStepOut = false;
        this.isCoverageRate = true;
    }

    /**
     * enable stop debug
     */
    public void enableStopDebug() {
        this.isCompile = false;
        this.isExecute = false;
        this.isStartDebug = false;
        this.isStopDebug = true;
        this.isContinueStep = true;
        this.isSingleStep = true;
        this.isStepIn = true;
        this.isStepOut = true;
        this.isCoverageRate = false;
    }

    /**
     * enable start anonymous block debug
     */
    public void enableStartAnonymous() {
        this.isCompile = false;
        this.isExecute = true;
        this.isStartDebug = true;
        this.isStopDebug = false;
        this.isContinueStep = false;
        this.isSingleStep = false;
        this.isStepIn = false;
        this.isStepOut = false;
        this.isCoverageRate = false;
    }

    /**
     * sub all true
     */
    public void subAllTrue() {
        this.isContinueStep = true;
        this.isSingleStep = true;
        this.isStepIn = true;
        this.isStepOut = true;
    }

    /**
     * sub all false
     */
    public void subAllFalse() {
        this.isContinueStep = false;
        this.isSingleStep = false;
        this.isStepIn = false;
        this.isStepOut = false;
    }
}
