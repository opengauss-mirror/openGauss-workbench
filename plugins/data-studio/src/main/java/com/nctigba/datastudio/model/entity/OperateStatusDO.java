/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import lombok.Data;

@Data
public class OperateStatusDO {
    private boolean isDebug;
    private boolean compile;

    private boolean execute;

    private boolean startDebug;

    private boolean stopDebug;

    private boolean continueStep;

    private boolean singleStep;

    private boolean startRun;

    private boolean stopRun;

    private boolean stepIn;

    private boolean stepOut;

    private boolean coverageRate;

    public OperateStatusDO() {
        init();
    }

    public void init() {
        this.compile = true;
        this.execute = false;
        this.startDebug = false;
        this.stopDebug = false;
        this.continueStep = false;
        this.singleStep = false;
        this.startRun = false;
        this.stopRun = false;
        this.stepIn = false;
        this.stepOut = false;
        this.coverageRate = false;
    }

    public void enableStopRun() {
        this.startRun = true;
        this.stopRun = false;
    }

    public void enableStartDebug() {
        this.compile = true;
        this.execute = true;
        this.startDebug = true;
        this.stopDebug = false;
        this.continueStep = false;
        this.singleStep = false;
        this.stepIn = false;
        this.stepOut = false;
        this.coverageRate = true;
    }

    public void enableStopDebug() {
        this.compile = false;
        this.execute = false;
        this.startDebug = false;
        this.stopDebug = true;
        this.continueStep = true;
        this.singleStep = true;
        this.stepIn = true;
        this.stepOut = true;
        this.coverageRate = false;
    }

    public void subAllTrue() {
        this.continueStep = true;
        this.singleStep = true;
        this.stepIn = true;
        this.stepOut = true;
    }

    public void subAllFalse() {
        this.continueStep = false;
        this.singleStep = false;
        this.stepIn = false;
        this.stepOut = false;
    }
}
