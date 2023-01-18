package com.nctigba.observability.instance.constants;


public class BaseConstance {

    public enum ServerStatus {
        RUNNING("running"), DISCONNECT("abnormal connection");

        ServerStatus(String msg) {
        }
    }

    public enum InstanceStatus {
        RUNNING("running"), DISCONNECT("abnormal connection");

        InstanceStatus(String msg) {
        }
    }
}
