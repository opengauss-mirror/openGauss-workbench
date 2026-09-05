# Issue 20 DataKit API Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 DataKit RC3 的 34 个指定 API 建立真实、可清理、可审计的 TestNG/Rest Assured 覆盖，并准备个人 fork 的 feature branch。

**Architecture:** 复用 `ApiTest` 既有配置、登录和请求规范；新增插件与 Agent 测试按域拆分，公共断言与轮询 helper 保持小而独立。所有外部环境和凭据通过运行时配置注入，测试结束自动清理 fixture。

**Tech Stack:** Java 17, Maven, TestNG 7.10.2, Rest Assured 5.5.0, DataKit 7.0.0-RC3, openEuler 20.03 LTS-SP1, openGauss 7.0.0-RC3。

## Global Constraints

- `BASELINE_SHA=7b408a266909231f65bebc6114a585c9f8c7693b`。
- `ISSUE_TARGET_DATABASE=openGauss 7.0.0-LTS`；`DEVELOPMENT_DATABASE_BASELINE=openGauss 7.0.0-RC3`；`RC3_IS_FORMAL_LTS=NO`；`FINAL_LTS_REGRESSION_REQUIRED=YES`。
- 只使用 `<REDACTED_VM_ADDRESS>`，不得连接旧 VM `<REDACTED_LEGACY_VM_ADDRESS>`。
- 不修改产品逻辑、现有 openGauss 全局配置或 #25/#26 工作区。
- 不提交真实密码、Token、SSH key、Authorization header；不使用 `--no-verify` 或 LFS/hook 绕过。

### Task 1: 建立 Phase A 证据目录与基线

**Files:**
- Create: `opensource-internship-doc/issue20/issue20_api_traceability.md`
- Create: `artifacts/issue20/baseline/README.md`
- Modify: `.gitignore` only if needed to exclude runtime artifacts and secrets

- [ ] **Step 1: 记录仓库与环境版本**

运行 `git fetch upstream`、`git rev-parse upstream/master`、`java -version`、`mvn -version`，只写入脱敏文本证据。

- [ ] **Step 2: 建立 34 项矩阵骨架**

按 Issue 提供的 P01-P14、A01-A20 建立完整行；未知字段写“待源码确认”，不得猜测。

- [ ] **Step 3: 执行 baseline**

运行 `Set-Location ApiTest; mvn test`，保存总数、通过、失败、跳过和失败分类，失败日志中脱敏。

- [ ] **Step 4: 提交 Phase A 文档**

运行 `git diff --check` 后提交 `docs: record issue 20 api test design and baseline plan`。

### Task 2: 完成源码追踪矩阵

**Files:**
- Modify: `opensource-internship-doc/issue20/issue20_api_traceability.md`

- [ ] **Step 1: 定位 Controller/handler/DTO/service/response**

使用 `rg` 搜索 `/system/plugins` 和 `/agent`，为每个 endpoint 填写源码路径、方法签名、请求字段、响应模型和应用码。

- [ ] **Step 2: 关联现有测试与处理结论**

记录 `SystemPluginControllerTest`、现有 Agent 测试或缺失证据，逐项选择 `KEEP/REFACTOR/DELETE/ADD`，并指定 fixture 与 cleanup。

- [ ] **Step 3: 校验矩阵完整性**

用脚本统计 P01-P14 与 A01-A20 均出现一次，缺失项阻止后续实现。

### Task 3: 插件只读与低副作用接口

**Files:**
- Modify: `ApiTest/src/test/java/org/opengauss/visualtool/api/SystemPluginControllerTest.java`
- Create: `ApiTest/src/test/java/org/opengauss/plugins/system/PluginReadOnlyApiTest.java`
- Modify: `ApiTest/src/test/resources/testng.xml`

- [ ] **Step 1: 为 count、extensions/list、get/{id}、isBaseOpsStart、list、listContent、unloadPluginsInfo 写明确响应断言**
- [ ] **Step 2: 对空列表、未知 id 和成功应用码分别断言，不只断言 HTTP 200**
- [ ] **Step 3: 运行该类测试并保存 `artifacts/issue20/plugin/` 证据**

### Task 4: 插件配置与生命周期接口

**Files:**
- Create: `ApiTest/src/test/java/org/opengauss/plugins/system/PluginConfigApiTest.java`
- Create: `ApiTest/src/test/java/org/opengauss/plugins/system/PluginLifecycleApiTest.java`
- Create: `ApiTest/src/test/java/org/opengauss/plugins/system/PluginFixture.java`
- Modify: `ApiTest/src/test/resources/testng.xml`

- [ ] **Step 1: 覆盖 pluginConfigData、getUnloadPluginUrl**
- [ ] **Step 2: 对 offline_install、online_install、start、stop、uninstall 实施 precheck-action-state-cleanup**
- [ ] **Step 3: 对失败 action 断言状态未半完成并执行清理**
- [ ] **Step 4: 运行插件 targeted suite，统计 14 项覆盖**

### Task 5: Agent fixture 与异步 helper

**Files:**
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentFixture.java`
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentPolling.java`
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentRequestFactory.java`

- [ ] **Step 1: 使用独立目录、端口、日志、PID、临时文件**
- [ ] **Step 2: 实现 deadline 轮询、终态集合、超时诊断和 finally cleanup**
- [ ] **Step 3: 验证 fixture 不触碰 5432、5433、`/opt/software/openGauss`**

### Task 6: Agent 20 API 测试

**Files:**
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentLifecycleApiTest.java`
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentDefinitionApiTest.java`
- Create: `ApiTest/src/test/java/org/opengauss/agent/AgentTaskApiTest.java`
- Modify: `ApiTest/src/test/resources/testng.xml`

- [ ] **Step 1: 覆盖 deregister、heartbeat、list、anomaly/checking、task/list、metrics/schema/template definition**
- [ ] **Step 2: 覆盖 install/start/stop/uninstall/updateAgentPort/upgrade 与 task callback/save**
- [ ] **Step 3: 每个异步接口断言最终状态、错误分支与 cleanup**
- [ ] **Step 4: 运行 Agent targeted suite，统计 20 项覆盖**

### Task 7: 完整回归与审计

**Files:**
- Create: `artifacts/issue20/SELF_TEST_REPORT.md`
- Create: `artifacts/issue20/traceability/requirement_traceability.md`
- Create: `artifacts/issue20/full-regression/`
- Modify: `ApiTest/README.md` for reproducible public API examples

- [ ] **Step 1: 运行 targeted、plugin、agent、full `mvn test`**
- [ ] **Step 2: 第二轮 full `mvn test` 并比较结果**
- [ ] **Step 3: 执行 residual-state、cleanup、secret scan、`git diff --check`、upstream diff audit**
- [ ] **Step 4: 填写真实机器可读报告与 34 项覆盖统计**

### Task 8: Push 与 PR 准备

**Files:**
- Modify: `artifacts/issue20/pr/` only with non-secret metadata

- [ ] **Step 1: 再次 fetch upstream 并检查 baseline 漂移**
- [ ] **Step 2: 运行最终测试后 commit**
- [ ] **Step 3: 正常 push 到 `origin feature/issue20-datakit-api-tests`**
- [ ] **Step 4: 用 `git ls-remote origin refs/heads/feature/issue20-datakit-api-tests` 验证远程 SHA**
- [ ] **Step 5: 准备包含“关联 openGauss 开源实习 Issue #20”的 PR 描述，不自动执行 `/intern-completed`**
