package com.nctigba.observability.instance.handler.os;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import com.nctigba.observability.instance.constants.OSType;
import com.nctigba.observability.instance.pool.SSHPoolManager;
import com.nctigba.observability.instance.util.SSHOperator;

@Component
public class NormalOSHandler implements OSHandler {
    @Override
    public String getOSType() {
        return OSType.DEFAULT.getOsType();
    }

    @Override
    public boolean testConnectStatus(String host, Integer port, String userName, String password) {
        SSHOperator ssh = SSHPoolManager.getSSHOperator(host, port, userName, password);
        return ObjectUtils.isNotEmpty(ssh) && ssh.isConnected();
    }
}
