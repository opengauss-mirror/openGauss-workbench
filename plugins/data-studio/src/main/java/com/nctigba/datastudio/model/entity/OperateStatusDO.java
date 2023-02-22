package com.nctigba.datastudio.model.entity;

import lombok.Data;

@Data
public class OperateStatusDO {
    private boolean isDebug;

    private boolean execute;

    private boolean startDebug;

    private boolean stopDebug;

    private boolean breakPointStep;

    private boolean singleStep;

    private boolean startRun;

    private boolean stopRun;

    private boolean stepIn;

    private boolean stepOut;

    public OperateStatusDO() {
        this.isDebug = true;
        init();
    }

    public void init() {
        this.execute = true;
        this.startDebug = false;
        this.stopDebug = false;
        this.breakPointStep = false;
        this.singleStep = false;
        this.startRun = false;
        this.stopRun = false;
        this.stepIn = false;
        this.stepOut = false;
    }

    public void enableStopRun() {
        this.startRun = true;
        this.stopRun = false;
    }

    public void enableStartDebug() {
        this.execute = true;
        this.startDebug = true;
        this.stopDebug = false;
        this.breakPointStep = false;
        this.singleStep = false;
        this.stepIn = false;
        this.stepOut = false;
    }

    public void enableStopDebug() {
        this.execute = false;
        this.startDebug = false;
        this.stopDebug = true;
        this.breakPointStep = true;
        this.singleStep = true;
        this.stepIn = true;
        this.stepOut = true;
    }

    public void allStatusFalse() {
        this.execute = false;
        this.startDebug = false;
        this.stopDebug = false;
        this.breakPointStep = false;
        this.singleStep = false;
    }
}
