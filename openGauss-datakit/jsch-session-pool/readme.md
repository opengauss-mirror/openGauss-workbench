# JSCH Session Pool

一个支持多主机多用户的JSCH Session连接池库，提供高效的SFTP/EXEC操作能力。

## 特性

- 多主机多用户Session池管理
- 连接复用，减少认证开销
- 大文件传输进度监控
- 并发安全设计
- 完善的连接池统计
- 支持公钥/密码两种认证方式

## 安装

在您的项目中添加Maven依赖：

```xml
<dependency>
    <groupId>org.opengauss</groupId>
    <artifactId>jsch-session-pool</artifactId>
    <version>xxx</version>
</dependency>
```

## 快速开始
- 基本使用
```java
// 创建配置
SftpConfig config = JschUtils.createBasicConfig("sftp.example.com", "user", "pass");
// 上传文件
SftpOperations.upload(config, "local.txt","/remote/local.txt",
    JschUtils.createConsoleProgressCallback("Upload"));
    // 下载文件
    SftpOperations.download(config, "/remote/largefile.zip","download.zip",
    JschUtils.createConsoleProgressCallback("Download"));
    // 关闭所有连接池（应用退出时调用）
    JschSessionPool.closeAllPools();
```

高级配置
```java
// 创建高性能连接池配置
GenericObjectPoolConfig<ChannelSftp> poolConfig = JschUtils.createHighPerfPoolConfig();

// 创建带密钥的配置
SftpConfig config = new SftpConfig("sftp.example.com", "user", null)
    .withPrivateKey("/path/to/private_key")
    .withKnownHosts("/path/to/known_hosts")
    .withTimeout(15000)
    .withPoolConfig(poolConfig);

// 使用自定义进度回调
ProgressCallback callback = new ProgressCallback() {
    @Override
    public void progress(long current, long total) {
        // 自定义进度处理
    }
    
    // ...其他方法实现
};

// 上传大文件
SftpOperations.upload(config, "largefile.zip", "/remote/largefile.zip", callback);
```

连接池监控
```java
// 获取连接池统计信息
String stats = JschSessionPool.getPoolStats();
System.out.println(stats);

// 输出示例
/*
SFTP Connection Pool Statistics:
========================================
Host: sftp1.example.com:22, User: user1, Auth: password
  Active: 2, Idle: 3, Waiters: 0
  Created: 5, Destroyed: 0
----------------------------------------
Host: sftp2.example.com:22, User: user2, Auth: key
  Active: 5, Idle: 2, Waiters: 1
  Created: 8, Destroyed: 1
----------------------------------------
*/
```

最佳实践
应用退出时关闭连接池

```java
Runtime.getRuntime().addShutdownHook(new Thread(JschSessionPool::closeAllPools));
```

合理配置连接池

```java
// 根据业务需求调整
GenericObjectPoolConfig<ChannelSftp> poolConfig = new GenericObjectPoolConfig<>();
poolConfig.setMaxTotal(20); // 最大连接数
poolConfig.setMaxIdle(10);  // 最大空闲连接
poolConfig.setMinIdle(5);   // 最小空闲连接
进度回调节流
```

```java
ProgressCallback callback = new ProgressCallback() {
private long lastUpdate = 0;

    @Override
    public void progress(long current, long total) {
        long now = System.currentTimeMillis();
        if (now - lastUpdate > 1000) { // 每秒更新一次
            updateProgressUI(current, total);
            lastUpdate = now;
        }
    }
    // ...其他方法
};
```

# 服务器SSH服务配置
调整 SSH 服务配置（/etc/ssh/sshd_config），放宽并发限制：

## 编辑配置
sudo vi /etc/ssh/sshd_config

## 调整以下参数（根据需求设置）
  MaxStartups 100:30:200   未认证连接：最大100，30%概率拒绝超过100的连接，最多200

  MaxSessions 50           每个连接的最大会话数

## 重启SSH服务
sudo systemctl restart sshd
