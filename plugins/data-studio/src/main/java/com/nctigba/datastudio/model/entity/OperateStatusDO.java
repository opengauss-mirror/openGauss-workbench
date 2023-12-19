/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  OperateStatusDO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/entity/OperateStatusDO.java
 *
 *  -------------------------------------------------------------------------
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
     * enable start debug package
     */
    public void enableStartDebugPackage() {
        this.isCompile = false;
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
