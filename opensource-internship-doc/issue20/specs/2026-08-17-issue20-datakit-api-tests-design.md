# Issue 20 DataKit API 测试补齐设计

## 目标

在不修改 DataKit 产品逻辑、不中断现有 RC3/openGauss 环境的前提下，为 Issue #20 指定的 14 个插件 API 与 20 个 Agent API 建立可审计的 TestNG + Rest Assured 自动化覆盖，并保留真实 baseline、失败分类、清理和安全证据。

## 冻结边界

- 代码仓库：`opengauss/openGauss-workbench`，个人 fork `yanfei01-2026/openGauss-workbench`。
- 基线：`7b408a266909231f65bebc6114a585c9f8c7693b`。
- 目标数据库：openGauss 7.0.0-LTS；开发验证：openGauss 7.0.0-RC3 build `f08516a2`，RC3 不等于正式 LTS。
- 只使用 `<REDACTED_VM_ADDRESS>`；禁止连接 `<REDACTED_LEGACY_VM_ADDRESS>`。
- 凭据只来自运行时环境或本地未跟踪配置；日志、报告和 Git 中不得出现真实密码、Token、SSH key 或 Authorization 值。

## 架构

测试继续复用 `ApiTest` 的 `AppConfigLoader`、`LoginUtils`、`Constants.getRequestSpecification()` 和 TestNG suite。新增用例按 `plugins`、`agent` 分层，公共请求/响应断言与脱敏日志放在现有 helper 边界内。插件生命周期用 precheck-action-state-cleanup 四段式；Agent 异步接口使用状态轮询和固定 deadline，不把 HTTP 200 作为唯一断言。

## API 追踪

`opensource-internship-doc/issue20/issue20_api_traceability.md` 为唯一追踪矩阵，逐项记录 endpoint、Controller/handler、DTO、service、response、现有测试、fixture、cleanup、最终测试和证据。源码中已删除、重命名或替代的接口必须记录证据并标记为不可伪造覆盖。

## 测试阶段

1. Phase A：读取框架与源码，建立 34 项矩阵，执行修改前 `cd ApiTest && mvn test`。
2. Phase B：使用公开 DataKit RC3 SUT，先完成无副作用插件接口，再完成配置与生命周期接口。
3. Phase C：为 Agent 使用独立目录、端口、日志、PID 和临时文件，完成 20 项接口与自动清理。
4. Phase D：targeted、plugin、agent、两轮 full regression、残留状态/清理审计、secret scan、`git diff --check`、upstream diff audit，最后 commit/push/PR。

## 验收口径

最终报告必须同时给出 `PLUGIN_API_TOTAL=14`、`AGENT_API_TOTAL=20`、`TOTAL_REQUIRED_API=34` 及真实 covered/pass/fail/skip 数字；失败必须区分 harness、环境、产品和测试断言问题。最终 upstream SHA、DataKit/Agent 版本、Java/Maven 版本、远程分支 SHA 和 PR URL 只填写实测值。
