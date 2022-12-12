package com.nctigba.observability.instance.handler.os;


public interface OSHandler {
    /**
     * Server operating system type
     *
     * @return String Operating system type
     */
    String getOSType();

    /**
     * Test server connection status
     *
     * @param host     ip
     * @param port     port
     * @param userName user
     * @param password password
     * @return boolean Connection Test Results
     */
    boolean testConnectStatus(String host, Integer port, String userName, String password);
}
