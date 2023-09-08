package com.nctigba.observability.sql.model.diagnosis.grab;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum GrabType {
    profile,
    offcputime,
    runqlen,
    runqlat,
    cachestat,
    memleak,
    xfsdist,
    xfsslower,
    biolatency,
    biosnoop,
    filetop,
    tcplife,
    tcptop,
    mpstatP,
    pidstat1,
    osParam
}