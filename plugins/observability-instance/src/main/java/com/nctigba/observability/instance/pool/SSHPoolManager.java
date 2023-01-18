package com.nctigba.observability.instance.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.util.SSHOperator;


public class SSHPoolManager {
    private static Map<String, SSHOperator> sshMap = new ConcurrentHashMap<>();

    private static String getKey(String host, Integer port, String userName) {
        return new StringBuilder().append(host).append((port != null) ? new StringBuilder().append("_").append(port).toString() : "").append("_").append(userName).toString();
    }

    /**
     * Delete ssh connection
     *
     * @param host     ip
     * @param port     prot
     * @param userName username
     */
    public static void removeSSHOperator(String host, Integer port, String userName) {
        String key = getKey(host, port, userName);
        SSHOperator ssh = sshMap.get(key);
        if (ssh == null) {
            return;
        }
        sshMap.remove(key);
        ssh.close();
    }

    /**
     * 获取服务器ssh连接
     *
     * @param host     ip
     * @param port     port
     * @param userName user
     * @param password password
     * @return SSHOperator ssh connection tool
     */
    public static SSHOperator getSSHOperator(String host, Integer port, String userName, String password) {
        return getSSHOperator(host, port, userName, false, null, password);
    }

    public static SSHOperator getSSHOperator(String host, Integer port, String userName, boolean isUsedPrivateKey, byte[] prvkeyContent, String password) {
        SSHOperator ssh = null;
        String key = getKey(host, port, userName);
        ssh = sshMap.get(key);
        if (ssh == null) {
            synchronized (SSHPoolManager.class) {
                if (sshMap.get(key) == null) {
                    if (isUsedPrivateKey) {
                        ssh = new SSHOperator(host, port, userName, prvkeyContent, password);
                    } else {
                        ssh = new SSHOperator(host, port, userName, password);
                    }
                    Session session = ssh.getSession();
                    if ((session != null) && (session.isConnected())) {
                        sshMap.put(key, ssh);
                    } else {
                        ssh.close();
                    }
                }
                ssh = sshMap.get(key);
            }
            return ssh;
        }
        Session session2 = ssh.getSession();
        if ((session2 != null) && (session2.isConnected())) {
            return ssh;
        }
        sshMap.remove(key);
        ssh.close();
        return null;
    }
}
