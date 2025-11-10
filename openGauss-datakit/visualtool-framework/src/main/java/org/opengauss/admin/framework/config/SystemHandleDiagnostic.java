/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.framework.config;

import jakarta.annotation.PreDestroy;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Handle Diagnostic Tool
 *
 * @author: wangchao
 * @Date: 2025/11/10 10:24
 * @Description: SystemHandleDiagnostic
 * @since 7.0.0-RC3
 **/
@Component
@EnableScheduling
public class SystemHandleDiagnostic {
    private static final Logger logger = LoggerFactory.getLogger(SystemHandleDiagnostic.class);
    private static final long UNIT_1024_1024 = 1024 * 1024L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_HANDLE_THRESHOLD = 10000;
    private static final int MID_HANDLE_THRESHOLD = 2000;
    private static final int MIN_HANDLE_THRESHOLD = 500;
    private static final int TRIGGER_REPORT_THRESHOLD = 100;
    private static final int TABLE_WIDTH = 120;
    private static final int TEXT_MAX_WIDTH = 70;
    private static final int MAX_DIAGNOSTIC_PER_MINUTE = 12;
    private static final long FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L;

    /**
     * Add lock to ensure thread safety for file operations and state changes
     */
    private final ReentrantLock fileOperationLock = new ReentrantLock();

    /**
     * Use AtomicBoolean to ensure thread-safe state control
     */
    private final AtomicBoolean monitoringEnabled = new AtomicBoolean(true);
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
    private final String currentWorkingDir = System.getProperty("user.dir");
    private final AtomicInteger diagnosticCount = new AtomicInteger(0);
    private volatile long lastReportedTotalHandles = 0L;
    private volatile long lastFiveMinuteReportTime = System.currentTimeMillis();

    /**
     * Save last handle statistics for comparison
     */
    private volatile Map<String, Long> lastHandleStats = new HashMap<>();
    @Value("${monitor.handle.enabled:true}")
    private boolean isConfigEnabled;
    @Value("${monitor.handle.report-dir:./logs/handle}")
    private String reportDir;
    private PrintWriter fileWriter;
    private String currentLogFile;

    /**
     * application started event notice , initialize SystemHandleDiagnostic
     *
     * @param event ContextRefreshedEvent
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Handle diagnostic initialized, monitoring: {}", isConfigEnabled);
        initializeFileWriter();
    }

    /**
     * Initialize independent file writer
     */
    private void initializeFileWriter() {
        fileOperationLock.lock();
        try {
            //  If shutting down, skip initialization
            if (shutdownInProgress.get()) {
                logger.warn("System is shutting down, skip file writer initialization");
                return;
            }
            // Create report directory
            File dir = new File(reportDir);
            if (!dir.exists()) {
                boolean isCreatedOk = dir.mkdirs();
                if (!isCreatedOk) {
                    logger.error("Failed to create handle report directory: {}", reportDir);
                    return;
                }
            }
            // Check and rotate log file (by date)
            String dateStr = LocalDateTime.now().format(FILE_TIME_FORMATTER);
            currentLogFile = reportDir + "/handle_report_" + dateStr + ".log";
            // Create file writer (append mode)
            fileWriter = new PrintWriter(new FileWriter(currentLogFile, true), true);
            logger.info("Handle diagnostic report will be written to: {}", currentLogFile);
        } catch (IOException e) {
            logger.error("Failed to initialize handle leak report file writer {}", e.getMessage());
        } finally {
            fileOperationLock.unlock();
        }
    }

    /**
     * Check and rotate log file
     */
    private void checkAndRotateLogFile() {
        fileOperationLock.lock();
        try {
            //  If shutting down or monitoring disabled, skip file rotation
            if (shutdownInProgress.get() || !monitoringEnabled.get()) {
                return;
            }
            String dateStr = LocalDateTime.now().format(FILE_TIME_FORMATTER);
            String newLogFile = reportDir + "/handle_report_" + dateStr + ".log";
            if (!newLogFile.equals(currentLogFile)) {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                currentLogFile = newLogFile;
                fileWriter = new PrintWriter(new FileWriter(currentLogFile, true), true);
                logger.info("Rotated handle report file to: {}", currentLogFile);
            }
        } catch (IOException e) {
            logger.error("Failed to rotate handle report file", e);
        } finally {
            fileOperationLock.unlock();
        }
    }

    /**
     * External interface: Close monitor
     */
    public void closeMonitor() {
        logger.info("Handle monitor shutdown requested");
        shutdownInProgress.set(true);
        monitoringEnabled.set(false);
        //  Wait for ongoing diagnostics to complete
        try {
            // Brief wait to ensure current diagnostic cycle completes
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for diagnostic completion");
        }
        destroy();
        logger.info("Handle monitor shutdown completed");
    }

    /**
     * External interface: Start monitor
     */
    public void startMonitor() {
        if (shutdownInProgress.get()) {
            logger.warn("System is in shutdown process, cannot start monitor");
            return;
        }
        monitoringEnabled.set(true);
        diagnosticCount.set(0);
        lastHandleStats.clear();
        // reinitialize independent file writer
        initializeFileWriter();
        logger.info("Handle monitor started");
    }

    /**
     * External interface: Check monitor status
     *
     * @return monitor status
     */
    public boolean isMonitorEnabled() {
        return monitoringEnabled.get() && !shutdownInProgress.get();
    }

    /**
     * External interface: Safely pause monitor (without releasing resources)
     */
    public void pauseMonitor() {
        monitoringEnabled.set(false);
        logger.info("Handle monitor paused");
    }

    /**
     * External interface: Resume monitor
     */
    public void resumeMonitor() {
        if (shutdownInProgress.get()) {
            logger.warn("System is in shutdown process, cannot resume monitor");
            return;
        }
        monitoringEnabled.set(true);
        logger.info("Handle monitor resumed");
    }

    @Scheduled(fixedDelay = 5000)
    private void scheduledDiagnostic() {
        // Safety check: If shutting down, monitor disabled, or config disabled, return directly
        if (shutdownInProgress.get() || !monitoringEnabled.get() || !isConfigEnabled || !isUnix()) {
            return;
        }
        //  Frequency control
        if (diagnosticCount.get() > MAX_DIAGNOSTIC_PER_MINUTE) {
            logger.debug("Diagnostic frequency limit reached, skipping this cycle");
            return;
        }
        diagnosticCount.incrementAndGet();
        diagnosticHandleLeak();
    }

    @Scheduled(fixedRate = 60000)
    private void resetDiagnosticCount() {
        if (!shutdownInProgress.get()) {
            diagnosticCount.set(0);
        }
    }

    private void diagnosticHandleLeak() {
        // Double-check to ensure state safety
        if (isUnix() && !shutdownInProgress.get() && monitoringEnabled.get()) {
            diagnosticUnixHandles();
        }
    }

    private void diagnosticUnixHandles() {
        try {
            long pid = ProcessHandle.current().pid();
            Path fdDir = Paths.get("/proc/" + pid + "/fd");
            if (Files.exists(fdDir)) {
                // Use collector for one-time processing, ensure resources are properly closed
                try (Stream<Path> stream = Files.list(fdDir)) {
                    // Check state again to prevent shutdown during stream operations
                    if (shutdownInProgress.get()) {
                        logger.debug("Shutdown in progress, aborting diagnostic");
                        return;
                    }
                    HandleStats result = stream.collect(HandleStats::new, HandleStats::accumulate,
                        HandleStats::combine);
                    generateMergedReport(pid, result.totalHandles, result.handleStats);
                }
            }
        } catch (IOException e) {
            // If shutting down, do not log errors
            if (!shutdownInProgress.get()) {
                logger.debug("Cannot diagnostic handles {}", e.getMessage());
            }
        }
    }

    private static class HandleStats {
        final Map<String, Long> handleStats = new HashMap<>();
        long totalHandles = 0L;

        /**
         * accumulate fdPath stats
         *
         * @param fdPath fdPath
         */
        void accumulate(Path fdPath) {
            totalHandles++;
            try {
                Path link = Files.readSymbolicLink(fdPath);
                handleStats.merge(link.toString(), 1L, Long::sum);
            } catch (IOException e) {
                logger.info("accumulate fdPath [{}] stats ignore {}", fdPath, e.getMessage());
            }
        }

        /**
         * combine stats
         *
         * @param other other
         */
        void combine(HandleStats other) {
            totalHandles += other.totalHandles;
            other.handleStats.forEach((k, v) -> handleStats.merge(k, v, Long::sum));
        }
    }

    /**
     * Generate merged report - plain text version (using relative paths)
     *
     * @param pid pid
     * @param totalHandles totalHandles
     * @param handleStats handleStats
     */
    private void generateMergedReport(long pid, long totalHandles, Map<String, Long> handleStats) {
        // State check: If shutting down or monitor disabled, do not generate report
        if (shutdownInProgress.get() || !monitoringEnabled.get()) {
            return;
        }
        // Check and rotate log file
        checkAndRotateLogFile();
        StringBuilder report = new StringBuilder();
        long changeHandles = totalHandles - lastReportedTotalHandles;
        // Basic information
        buildBasicInformation(pid, totalHandles, report, changeHandles);
        // Memory information
        buildMemoryInformation(report);
        // Handle type statistics
        buildHandleTypeStatistics(handleStats, report);
        // Analyze JAR file handles
        Map<String, JarFileInfo> jarHandles = analyzeJarHandles(handleStats);
        long totalJarHandles = jarHandles.values().stream().mapToLong(JarFileInfo::getTotalHandles).sum();
        buildAnalyzeJarFileHandlesReport(totalHandles, report, jarHandles, totalJarHandles);
        long currentTime = System.currentTimeMillis();
        boolean shouldReport = false;
        if (Math.abs(changeHandles) > TRIGGER_REPORT_THRESHOLD) {
            shouldReport = true;
            logger.debug("Trigger report due to handle count change: {} -> {}", lastReportedTotalHandles, totalHandles);
        }
        if (currentTime - lastFiveMinuteReportTime >= FIVE_MINUTES_IN_MILLIS) {
            shouldReport = true;
            lastFiveMinuteReportTime = currentTime;
            logger.debug("Trigger 5-minute periodic report");
        }
        if (shouldReport) {
            writeToReportFile(report.toString());
            // Also record summary in regular logs (optional)
            summaryReport(totalHandles, totalJarHandles, changeHandles);
            // Update last reported total handles and handle statistics
            lastReportedTotalHandles = totalHandles;
            lastHandleStats = new HashMap<>(handleStats);
        }
    }

    private void summaryReport(long totalHandles, long totalJarHandles, long changeHandles) {
        String summary = String.format(Locale.getDefault(),
            "Handle diagnostic completed - Total: %s, JAR: %s, Change: %s", formatNumber(totalHandles),
            formatNumber(totalJarHandles), changeHandles);
        // Select log level based on severity
        if (totalHandles > MAX_HANDLE_THRESHOLD) {
            logger.error(summary);
        } else if (totalHandles > MID_HANDLE_THRESHOLD) {
            logger.warn(summary);
        } else {
            logger.info(summary);
        }
    }

    private void buildAnalyzeJarFileHandlesReport(long totalHandles, StringBuilder report,
        Map<String, JarFileInfo> jarHandles, long totalJarHandles) {
        report.append("JAR File Handle Analysis:\n");
        report.append(String.format(Locale.getDefault(), "  Total JAR Files: %d\n", jarHandles.size()));
        report.append(String.format(Locale.getDefault(), "  Total JAR Handles: %s\n", formatNumber(totalJarHandles)));
        report.append("-".repeat(TABLE_WIDTH)).append("\n");
        // Warnings and risk analysis
        List<String> warnings = generateWarnings(totalJarHandles, jarHandles, totalHandles);
        if (!warnings.isEmpty()) {
            report.append("Risk Analysis:\n");
            warnings.forEach(warning -> report.append("  ").append(warning).append("\n"));
        }
        report.append("=".repeat(TABLE_WIDTH));
    }

    private void buildHandleTypeStatistics(Map<String, Long> handleStats, StringBuilder report) {
        report.append("Handle Type Statistics Top 15:\n");
        List<Map.Entry<String, Long>> topEntries = handleStats.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(15)
            .toList();
        // Calculate changes
        Map<String, Long> changes = calculateChanges(handleStats);
        for (int i = 0; i < topEntries.size(); i++) {
            Map.Entry<String, Long> entry = topEntries.get(i);
            String fileType = getFileTypeIcon(entry.getKey());
            // Use relative path
            String fileName = truncatePath(toRelativePath(entry.getKey()), TEXT_MAX_WIDTH);
            long currentCount = entry.getValue();
            long change = changes.getOrDefault(entry.getKey(), 0L);
            String changeIndicator = formatChangeIndicator(change);
            report.append(String.format(Locale.getDefault(), "  %2d. %s %-70s %10s %s\n", i + 1, fileType, fileName,
                formatNumber(currentCount), changeIndicator));
        }
        report.append("-".repeat(TABLE_WIDTH)).append("\n");
    }

    private void buildBasicInformation(long pid, long totalHandles, StringBuilder report, long changeHandles) {
        report.append("=".repeat(TABLE_WIDTH)).append("\n");
        report.append(centerString("File Handle Diagnostic Report", TABLE_WIDTH)).append("\n");
        report.append("=".repeat(TABLE_WIDTH)).append("\n");
        report.append(String.format(Locale.getDefault(), "Process ID: %d\n", pid));
        report.append(String.format(Locale.getDefault(), "Total Handles: %s\n", formatNumber(totalHandles)));
        report.append(
            String.format(Locale.getDefault(), "Handle %s Count: %s\n", changeHandles > 0 ? "Increase" : "Decrease",
                Math.abs(changeHandles)));
        report.append(
            String.format(Locale.getDefault(), "Diagnostic Time: %s\n", LocalDateTime.now().format(TIME_FORMATTER)));
    }

    private static void buildMemoryInformation(StringBuilder report) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / UNIT_1024_1024;
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / UNIT_1024_1024;
        report.append(String.format(Locale.getDefault(), "Heap Memory Usage: %dM / %dM\n", usedMemory, maxMemory));
        report.append("-".repeat(TABLE_WIDTH)).append("\n");
    }

    /**
     * Calculate handle count changes
     *
     * @param currentHandleStats currentHandleStats
     * @return changes
     */
    private Map<String, Long> calculateChanges(Map<String, Long> currentHandleStats) {
        Map<String, Long> changes = new HashMap<>();
        if (lastHandleStats.isEmpty()) {
            // First run, all changes are 0
            currentHandleStats.keySet().forEach(key -> changes.put(key, 0L));
            return changes;
        }
        // Calculate changes for existing files
        currentHandleStats.forEach((key, currentCount) -> {
            long lastCount = lastHandleStats.getOrDefault(key, 0L);
            changes.put(key, currentCount - lastCount);
        });
        // Mark deleted files (although they won't appear in top15, record for completeness)
        lastHandleStats.keySet()
            .stream()
            .filter(key -> !currentHandleStats.containsKey(key))
            .forEach(key -> changes.put(key, -lastHandleStats.get(key)));
        return changes;
    }

    /**
     * Format change indicator
     *
     * @param change change
     * @return result
     */
    private String formatChangeIndicator(long change) {
        if (change > 0) {
            return String.format(Locale.getDefault(), "(+%d)", change);
        } else if (change < 0) {
            return String.format(Locale.getDefault(), "(%d)", change);
        } else {
            return "";
        }
    }

    /**
     * Analyze JAR file handles
     *
     * @param handleStats handleStats
     * @return result
     */
    private Map<String, JarFileInfo> analyzeJarHandles(Map<String, Long> handleStats) {
        Map<String, JarFileInfo> jarHandles = new HashMap<>();
        handleStats.entrySet().stream().filter(entry -> entry.getKey().endsWith(".jar")).forEach(entry -> {
            String fullPath = entry.getKey();
            File jarFile = new File(fullPath);
            String jarName = jarFile.getName();
            String jarDir = jarFile.getParent();
            JarFileInfo info = jarHandles.getOrDefault(jarName, new JarFileInfo(jarName, jarDir));
            info.addHandle(fullPath, entry.getValue());
            jarHandles.put(jarName, info);
        });
        return jarHandles;
    }

    /**
     * Generate warning messages
     *
     * @param totalJarHandles totalJarHandles
     * @param jarHandles jarHandles
     * @param totalHandles totalHandles
     * @return warning result
     */
    private List<String> generateWarnings(long totalJarHandles, Map<String, JarFileInfo> jarHandles,
        long totalHandles) {
        List<String> warnings = new ArrayList<>();
        // JAR句柄泄漏警告
        if (totalJarHandles > MAX_HANDLE_THRESHOLD) {
            warnings.add("Severe warning: JAR file handle leak risk detected!");
            buildWarningTop3File(totalJarHandles, MAX_HANDLE_THRESHOLD, jarHandles, warnings);
            warnings.add(
                "Suggestion: Immediately check resource management code, ensure JAR files are properly closed!");
        } else if (totalJarHandles > MID_HANDLE_THRESHOLD) {
            warnings.add("Note: JAR file handle count is high, recommended to monitor");
            buildWarningTop3File(totalJarHandles, MID_HANDLE_THRESHOLD, jarHandles, warnings);
        } else if (totalJarHandles > MIN_HANDLE_THRESHOLD) {
            warnings.add("Note: JAR file handle count is high, recommended to monitor");
        } else {
            warnings.add("Note: JAR file handle count is health, recommended to monitor");
        }
        // Total handle count warning
        if (totalHandles > MAX_HANDLE_THRESHOLD) {
            warnings.add("Warning: Total handle count is abnormally high, possible system resource leak");
        }
        return warnings;
    }

    private void buildWarningTop3File(long totalJarHandles, long threshold, Map<String, JarFileInfo> jarHandles,
        List<String> warnings) {
        warnings.add(
            String.format(Locale.getDefault(), "  Current JAR Handles: %s, Safety Threshold: %s, Exceeded: %d%%",
                formatNumber(totalJarHandles), formatNumber(threshold), totalJarHandles * 100 / threshold));
        // Find main leak sources
        jarHandles.entrySet()
            .stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().getTotalHandles(), e1.getValue().getTotalHandles()))
            .limit(3)
            .forEach(entry -> {
                JarFileInfo info = entry.getValue();
                warnings.add(
                    String.format(Locale.getDefault(), "  Main Leak Source: %s (%s handles)", info.getJarName(),
                        formatNumber(info.getTotalHandles())));
            });
    }

    /**
     * Convert absolute path to relative path
     *
     * @param absolutePath absolutePath
     * @return toRelativePath
     */
    private String toRelativePath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return absolutePath;
        }
        // If it's a special file (like anon_inode), return directly
        if (absolutePath.startsWith("anon_inode:") || absolutePath.startsWith("socket:") || absolutePath.startsWith(
            "pipe:")) {
            return absolutePath;
        }
        // If it's a /proc filesystem path, keep as is
        if (absolutePath.startsWith("/proc/")) {
            return absolutePath;
        }
        // Convert to path relative to current working directory
        if (absolutePath.startsWith(currentWorkingDir)) {
            String relativePath = absolutePath.substring(currentWorkingDir.length());
            // Remove leading slash
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            // If relative path is empty, it means current directory
            if (relativePath.isEmpty()) {
                return ".";
            }
            return relativePath;
        }
        // If path is not under current working directory, try to extract filename
        if (absolutePath.contains("/")) {
            // Return filename part
            return absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
        }
        return absolutePath;
    }

    private String centerString(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private String formatNumber(long number) {
        return String.format("%,d", number);
    }

    private String truncatePath(String path, int maxLength) {
        if (path.length() <= maxLength) {
            return path;
        }
        if (path.contains("/")) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.length() > maxLength - 10) {
                return "..." + fileName.substring(0, maxLength - 10);
            }
            return "..." + path.substring(path.length() - maxLength + 3);
        }
        return "..." + path.substring(path.length() - maxLength + 3);
    }

    private String getFileTypeIcon(String fileName) {
        if (fileName.endsWith(".jar")) {
            return "[JAR]";
        }
        if (fileName.contains("/proc/")) {
            return "[SYS]";
        }
        if (fileName.contains("/logs/")) {
            return "[LOG]";
        }
        if (fileName.contains("/data/")) {
            return "[DB]";
        }
        if (fileName.contains("anon_inode")) {
            return "[INODE]";
        }
        return "[FILE]";
    }

    private boolean isUnix() {
        return !System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Close file writer when destroying
     */
    @PreDestroy
    public void destroy() {
        shutdownInProgress.set(true);
        monitoringEnabled.set(false);
        fileOperationLock.lock();
        try {
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
                logger.info("Handle diagnostic file writer closed");
            }
        } finally {
            fileOperationLock.unlock();
        }
    }

    /**
     * Write to independent log file
     *
     * @param content content
     */
    private void writeToReportFile(String content) {
        fileOperationLock.lock();
        try {
            if (fileWriter != null && !shutdownInProgress.get()) {
                fileWriter.println(content);
                fileWriter.flush();
            }
        } finally {
            fileOperationLock.unlock();
        }
    }

    /**
     * JAR file information inner class
     */
    @Getter
    private static class JarFileInfo {
        private final String jarName;
        private final String jarDir;
        private final Map<String, Long> pathStats = new HashMap<>();
        private long totalHandles = 0L;

        /**
         * build jarFileInfo obj
         *
         * @param jarName jar name
         * @param jarDir jar Dir
         */
        public JarFileInfo(String jarName, String jarDir) {
            this.jarName = jarName;
            this.jarDir = jarDir;
        }

        /**
         * add jar handle
         *
         * @param fullPath full path
         * @param count count
         */
        public void addHandle(String fullPath, long count) {
            pathStats.put(fullPath, count);
            totalHandles += count;
        }
    }
}