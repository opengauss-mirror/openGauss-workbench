# Issue #20 自验报告

## 版本与边界

- `BASELINE_SHA=7b408a266909231f65bebc6114a585c9f8c7693b`
- `DATakit_VERSION=7.0.0-RC3`
- `OPEN_GAUSS_BUILD=f08516a2`
- `DEVELOPMENT_DATABASE_BASELINE=openGauss 7.0.0-RC3`
- `RC3_IS_FORMAL_LTS=NO`
- `FINAL_LTS_REGRESSION_REQUIRED=YES`
- SUT：隔离 VM `<REDACTED_VM_ADDRESS>`，未连接 `<REDACTED_LEGACY_VM_ADDRESS>`

所有密码、Token、SSH 信息和运行时配置均通过本地/远程 ignored 文件注入，没有进入仓库。

## 真实执行结果

### Baseline

在新增测试前，RC3 baseline 为：

```text
BASELINE_TEST_TOTAL=130
BASELINE_TEST_FAIL=8
BASELINE_TEST_ERROR=0
BASELINE_TEST_SKIP=32
BASELINE_CLASSIFICATION=EXISTING_FIXTURE_ENVIRONMENT_FAILURES
```

### Issue #20 targeted tests

```text
PLUGIN_READONLY_TESTS=4
PLUGIN_READONLY_PASS=4
PLUGIN_EXISTING_CLASS_TESTS=11
PLUGIN_EXISTING_CLASS_PASS=11
P09_ONLINE_INSTALL=FULL_PASS
AGENT_SAFE_TESTS=8
AGENT_SAFE_PASS=8
A13_A15_A17_DEFINITION_PERSISTENCE=FULL_PASS
DEFINITION_CLEANUP=PASS
```

### Full suite

加入 Issue #20 测试后的真实 RC3 TestNG 回归：

```text
FULL_TEST_TOTAL=142
FULL_TEST_FAIL=7
FULL_TEST_ERROR=0
FULL_TEST_SKIP=32
FULL_TEST_NEW_ISSUE20_TEST_FAILURES=0
FULL_RUN_1=PASS_WITH_EXPECTED_RC3_ANOMALY_UNDER_INVESTIGATION
FULL_RUN_2=PASS_WITH_EXPECTED_RC3_ANOMALY_UNDER_INVESTIGATION
```

7 个失败均来自既有 Host/JDBC/HostTag 测试的外部 fixture，不属于新增 Plugin/Agent API 失败；新增测试没有增加失败数。完整原始日志保存在本地 ignored 路径 `artifacts/issue20/full-regression/`，不提交到 Git。

### Agent disposable 正向探针

首次诊断发现 DataKit 在当前隔离 SUT 上生成的 Agent 配置为 `agent.server: ''`。这不是 Agent JAR、openGauss 或 SSH 的兼容性错误：Agent 的既有心跳逻辑在连续无法访问空目标后调用 `Quarkus.asyncExit(1)`，因此表现为健康检查窗口内 `STOP`。

测试夹具随后使用冻结版本公开的 `GET/PUT /system/setting` 接口，将当前 DataKit 系统设置完整备份后，仅在 disposable 生命周期内设置 `serverHost=127.0.0.1`，并在 `@AfterClass(alwaysRun=true)` 恢复原值。真实 GREEN 结果：

```text
AGENT_FIXTURE_SSH_PASSWORD_AUTH=PASS
AGENT_JAVA_CHECK=PASS
AGENT_SERVER_HOST_FIXTURE=PASS
AGENT_GENERATED_SERVER=https://127.0.0.1:9494
AGENT_LIFECYCLE_TESTS=12
AGENT_LIFECYCLE_PASS=11
AGENT_START_TASK_EXECUTED_ANOMALY_UNDER_INVESTIGATION=1
AGENT_INSTALL_START_STOP=PASS
AGENT_PORT_UPDATE=PASS
AGENT_UPGRADE_CHECKSUM_PATH=PASS
AGENT_UNINSTALL_RECORD_REMOVAL=PASS
AGENT_FIXTURE_CLEANUP=PASS
```

任务接口探针另外验证了 `/agent/taskInstance/save` 可以创建 disposable task。随后使用同一 task 调用
`/agent/start/task`，冻结 RC3 返回 HTTP 200、业务码 500；服务端日志证实
`AgentTaskManager.startAgentTaskByTaskId()` 在 `base_host_info`（OSHI）模板上因
`clusterConfig == null` 仍调用 `getDbPassword()`，形成真实的 `NullPointerException`。
这被记录为产品运行时兼容结果，而不是测试断言失败；测试不会修改 DataKit 产品逻辑来绕过它：

```text
A11_TASK_INSTANCE_SAVE=PASS
A07_START_TASK=REPRODUCIBLE_ANOMALY_UNDER_INVESTIGATION
A07_HTTP_STATUS=200
A07_APPLICATION_CODE=500
A07_ERROR_MESSAGE=clusterConfig_NULL_NPE
```

临时 Linux 用户、Agent 目录、Agent 进程、JDK 目录和临时 `java` 链接均在探针结束后确认清理；没有修改 SSHD、pg_hba、OpenSSL 或 openGauss 全局配置。

该探针还确认：`agentId` 必须等于对应 `ops_host.host_id`；任意字符串会在 `AgentSshLoginService` 的 host 查询中触发驱动类型转换错误。测试已修正为使用真实 host id。

## 覆盖结论

```text
PLUGIN_API_TOTAL=14
PLUGIN_API_FULLY_COVERED=14
AGENT_API_TOTAL=20
AGENT_API_FULLY_COVERED=19
AGENT_API_PARTIALLY_COVERED=0
AGENT_API_EXECUTED_ANOMALY_UNDER_INVESTIGATION=1
AGENT_API_UNRUN=0
TOTAL_REQUIRED_API=34
TOTAL_FULLY_COVERED=33
TOTAL_PARTIALLY_COVERED=0
TOTAL_EXECUTED_ANOMALY_UNDER_INVESTIGATION=1
TOTAL_UNRUN=0
TOTAL_ENDPOINT_EXECUTED=34
```

`/system/plugins/online_install` 已使用冻结 RC3 SQL 中的官方 OBS 插件 URL 完成真实安装、列表/详情验证和卸载恢复。
Agent 的 `/agent/start/task` 已在真实 disposable task 上执行，但冻结 RC3 存在上述 NPE；
`/agent/taskInstance/save` 已独立 PASS。`/agent/task/callback/start` 已在已安装 Agent、空 task 集合下通过。
三个 definition save 接口已完成非空保存、字段验证、停止 DataKit 后的精确 IntarkDB 删除、重启和残留审计，均为 FULL_PASS。

`A07_STATUS=REPRODUCIBLE_ANOMALY_UNDER_INVESTIGATION`
`A07_PRODUCT_DEFECT_CONFIRMED=NO`
`A07_MENTOR_REVIEW_REQUIRED=YES`
`A07_SOURCE_EVIDENCE=AgentTaskManager.startAgentTaskByTaskId -> clusterConfig.getDbPassword()`
`HARNESS_ASSERTION_BUG=RESOLVED`

### 剩余接口边界与源码证据

`P09=FULL_PASS`：冻结 RC3 的 `SystemPluginController.onlineInstall(PluginDownloadDTO)` 使用
官方 OBS URL、有效 WebSocket business session、状态轮询和卸载恢复完成真实验证；未使用
`base-ops` 或伪造包。

`A13=A15=A17=FULL_PASS`：测试创建唯一 disposable 定义，验证非空保存和字段，再停止
DataKit，使用 IntarkDB 精确删除对应 metrics/schema/template 行，确认删除计数为 1、残留为 0，
随后重启 DataKit 并确认 HTTP 200。

独立 Agent 生命周期套件不是默认 `mvn test` 的一部分，原因是它需要运行时 SSH fixture。真实执行方式：

```text
cd ApiTest
mvn -Dtest=org.opengauss.agent.AgentLifecycleApiTest -Dsurefire.suiteXmlFiles= test
```

所需运行时变量为 `APITEST_CONFIG`、`ISSUE20_AGENT_ID`、`ISSUE20_AGENT_USER`、
`ISSUE20_AGENT_PASSWORD`；密码只存在于 disposable 进程环境，不写入报告或日志。

本轮拆分后的重新执行记录：

```text
POST_SPLIT_TEST_COMPILE=PASS
POST_SPLIT_RUNTIME_REEXECUTION=PASS_FOR_AGENT_DISPOSABLE_SUITE
POST_SPLIT_AGENT_TESTS=12
POST_SPLIT_AGENT_PASS=11
POST_SPLIT_AGENT_EXECUTED_ANOMALY_UNDER_INVESTIGATION=1
POST_SPLIT_BLOCKER=SSH_DIRECT_TCPIP_ADMINISTRATIVELY_PROHIBITED_RESOLVED_BY_VM_LOCAL_EXECUTION
POST_SPLIT_API_RESULT=REAL_RC3_RESULT_CAPTURED
```

远端 SSH 日志显示本机转发请求被服务端以 `administratively prohibited` 拒绝；根据任务约束未修改 SSHD。随后改用同一 VM 的 Linux Temurin JDK、临时 Maven 本地仓库和 DataKit localhost 直接运行 ApiTest，12 个 Agent 生命周期测试真实执行（11 PASS，A07 compatibility probe 记录 1 个可复现异常，当前仍在调查）。历史残留的 `issue20a*` disposable 用户已逐一终止并删除；远端 `exit 127` 根因是误用了 Windows JDK 压缩包；已改用 Linux JDK，未修改系统全局 profile。

`AGENT_RUNTIME_INITIAL_DIAG=HARNESS_CONFIGURATION_MISSING_SERVER_HOST`

## 重要契约证据

- `/agent/task/list` 的 frozen RC3 返回模型是 `Map<String,List<AgentTaskConfig>>`，空环境返回 `{}`，不是数组。
- `/agent/heartbeat` 对未知 agent id 返回成功码但不创建安装记录，源码路径已在追踪矩阵中记录。
- Plugin 生命周期测试在隔离 DataKit 中完成停止、启动、卸载和 offline reinstall，并恢复 `base-ops`。

## Git 发布状态

```text
LOCAL_CHECKPOINT_COMMIT=0f2d2ffe
UPSTREAM_BASELINE_DRIFT=NO
PUSH_STATUS=PASS
REMOTE_BRANCH=origin/feature/issue20-datakit-api-tests
REMOTE_SHA=0f2d2ffe450e72cd5a4fe0ea9eb8357c49ad2382
LOCAL_REMOTE_SHA_MATCH=YES
REMOTE_SHA_NOTE=本报告记录最终证据校正提交对应的远程分支 SHA
PR_READY=YES
```

GitCode 个人 fork 的 feature branch 已成功推送，且本地 HEAD 与远程 SHA 一致。未使用 hook/LFS 绕过，也未把浏览器登录态当作 CLI 凭据。最终证据校正、secret scan、diff audit 和远程 SHA 校验均已完成；当前只保留创建 upstream PR 这一步供后续人工确认。A07 保持为 `REPRODUCIBLE_ANOMALY_UNDER_INVESTIGATION`，`A07_PRODUCT_DEFECT_CONFIRMED=NO`，当前等待导师确认 task 构造语义和完整日志。
