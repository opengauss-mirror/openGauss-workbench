package com.tools.monitor.service;

/**
 * SnowFlake
 *
 * @author liu
 * @since 2022-10-01
 */
public class SnowFlake {
    private final static long START_STAMP = 1480166465631L;

    private final static long SEQUENCE_BIT = 12;

    private final static long MACHINE_BIT = 5;

    private final static long DATA_CENTER_BIT = 5;

    private final static long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);

    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    private final static long MACHINE_LEFT = SEQUENCE_BIT;

    private final static long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    private final static long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;

    private final long dataCenterId;

    private final long machineId;

    private long sequence = 0L;

    private long lastStamp = -1L;

    public SnowFlake(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId can't be greater than MAX_DATA_CENTER_NUM or less than " +
                    "0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            sequence = 0L;
        }
        lastStamp = currStamp;
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }
}

