/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  PrometheusMainRunner.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/config/runner/PrometheusMainRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config.runner;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.service.PrometheusService;
import com.nctigba.observability.instance.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Check if the main Prometheus is installed. if not, install
 *
 * @since 2024/2/2 17:21
 */
@Component
@Slf4j
@Profile("!dev")
public class PrometheusMainRunner implements ApplicationRunner {
    private static final String INSTALL_PATH = "data/prometheus-main";
    private static final String TAR = ".tar.gz";
    private static final String STARTUP_SH = "classpath:sh/run_prometheus.sh";

    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public void run(ApplicationArguments args) {
        Long count = envMapper.selectCount(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType,
            NctigbaEnvDO.envType.PROMETHEUS_MAIN));
        if (count > 0) {
            return;
        }
        try {
            // install the main Prometheus
            Path path = Path.of(INSTALL_PATH);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            String archStr = System.getProperty("os.arch");
            Resource[] resources =
                resourcePatternResolver.getResources("classpath*:pkg/prometheus-**-" + archStr + TAR);
            if (resources.length == 0) {
                throw new CustomException("The Prometheus install package is not exist");
            }

            int port = checkPortAndGet();

            Resource resource = resources[0];
            String filename = resource.getFilename();
            String promDir = path.toAbsolutePath() + "/" + filename.substring(0, filename.length() - TAR.length());
            Resource startSh = resourcePatternResolver.getResource(STARTUP_SH);
            if (!Files.exists(Path.of(promDir))) {
                // install
                install(resource, path);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("port", port);
                paramMap.put("storageDays", "7d");
                addStartupSh(startSh, promDir, paramMap);
                chmodFile(promDir);
            }

            // save env
            NctigbaEnvDO nctigbaEnv = new NctigbaEnvDO();
            nctigbaEnv.setPort(port).setPath(promDir)
                .setType(NctigbaEnvDO.envType.PROMETHEUS_MAIN);
            envMapper.insert(nctigbaEnv);

            // startup
            CommonUtils.processCommand(new File(promDir), "/bin/sh", startSh.getFilename(), "start");
            prometheusService.checkHealthStatus(nctigbaEnv);
        } catch (IOException | InterruptedException | CustomException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addStartupSh(Resource resource, String promDir, Map<String, Object> paramMap)
        throws IOException, InterruptedException {
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            StringBuilder strBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(CommonUtils.parseText(line, paramMap));
                strBuilder.append(System.lineSeparator());
            }
            ByteArrayInputStream inStream =
                new ByteArrayInputStream(strBuilder.toString().getBytes(Charset.defaultCharset()));
            File file = new File(promDir, resource.getFilename());
            inStream.transferTo(new FileOutputStream(file));
            inStream.close();
        }
    }

    private void chmodFile(String file) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (Files.isDirectory(Path.of(file))) {
            processBuilder.command("chmod", "-R", "777", file);
        } else {
            processBuilder.command("chmod", "777", file);
        }
        Process process = processBuilder.start();
        boolean isExit = process.waitFor(2L, TimeUnit.SECONDS);
        if (!isExit) {
            throw new CustomException("chmod 777 fail!");
        }
    }

    private void install(Resource resource, Path targetPath) {
        try (InputStream fi = resource.getInputStream();
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {
            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                Path targetDirResolved = targetPath.resolve(entry.getName());
                Path newPath = targetDirResolved.normalize();

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Path parent = newPath.getParent();
                    if (parent != null && Files.notExists(parent)) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CustomException("install fail");
        }
    }

    private int checkPortAndGet() throws IOException, InterruptedException {
        int port = 9090;
        // Is the port in use
        boolean isUsed = true;
        for (int i = 0; i < 10; i++) {
            String result = CommonUtils.processCommand("/bin/sh", "-c",
                "netstat -tuln | grep :" + port + " && echo true || echo false");
            isUsed = result.contains("true");
            if (isUsed) {
                port++;
                continue;
            }
            break;
        }
        if (isUsed) {
            throw new CustomException("The port is used:" + port);
        }
        return port;
    }
}
