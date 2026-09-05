# Issue 20 Closeout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Close the remaining Issue #20 API coverage and evidence gaps without changing DataKit product logic, then rerun all required validation gates.

**Architecture:** Keep API tests in `ApiTest` and use disposable fixtures only. Extend definition-save tests only when the RC3 API exposes a safe create/list/delete lifecycle; otherwise record a sourced partial result. Treat `/system/plugins/online_install` as PASS only with a real public, reversible package fixture.

**Tech Stack:** Java, Maven, TestNG, REST-assured, DataKit 7.0.0-RC3, openGauss 7.0.0-RC3.

## Global Constraints

- `BASELINE_SHA=7b408a266909231f65bebc6114a585c9f8c7693b`
- Do not modify frozen DataKit product logic or global VM security/database configuration.
- Do not access `<REDACTED_LEGACY_VM_ADDRESS>`.
- Secrets remain runtime-only and must not enter Git or logs.
- No `--no-verify`, hook bypass, or destructive cleanup outside disposable fixtures.
- Do not create an upstream PR until `PR_READY=YES`.

### Task 1: Normalize evidence documents

**Files:**
- Modify: `opensource-internship-doc/issue20/SELF_TEST_REPORT.md`
- Modify: `opensource-internship-doc/issue20/issue20_api_traceability.md`

- [ ] Replace stale `REMOTE_SHA` with the measured remote SHA and document the separate lifecycle integration command/environment.
- [ ] Preserve the distinction between native failure, partial coverage, unrun fixture, and harness failure.
- [ ] Verify the documents contain no secret values.

### Task 2: Inspect definition persistence and cleanup

**Files:**
- Inspect: `ApiTest/src/test/java/org/opengauss/agent/AgentReadOnlyApiTest.java`
- Inspect: `AgentTaskDefinitionController`, definition services, entities, mappers, and schema resources.

- [ ] Trace A13/A15/A17 save/list/delete behavior from controller through persistence.
- [ ] Identify an existing safe delete or disposable namespace cleanup path.
- [ ] Implement non-empty save/list assertions only when cleanup is provably safe; otherwise retain PARTIAL with source evidence.

### Task 3: Resolve P09 fixture boundary

**Files:**
- Inspect: `SystemPluginController`, `PluginDownloadDTO`, plugin metadata/resources, and RC3 package contents.
- Modify: plugin test and evidence docs only if a real reversible fixture is found.

- [ ] Locate a public RC3-compatible plugin package with stable URL and uninstall path.
- [ ] Run precheck → online_install → poll → list/get verification → uninstall → cleanup.
- [ ] If unavailable, record `P09=BLOCKED_PUBLIC_FIXTURE_UNAVAILABLE` and do not fake a positive test.

### Task 4: Full verification and handoff gate

**Files:**
- Modify: `opensource-internship-doc/issue20/SELF_TEST_REPORT.md`
- Modify: `opensource-internship-doc/issue20/issue20_api_traceability.md`

- [ ] Run targeted plugin and Agent suites, then two full `mvn test` runs.
- [ ] Run residual-state audit, cleanup audit, secret scan, and `git diff --check`.
- [ ] Commit and push normally; verify local and remote SHA match.
- [ ] Set `PR_READY=YES` only if all required gates and evidence are satisfied; otherwise keep `PR_READY=NO` with exact blocker.
