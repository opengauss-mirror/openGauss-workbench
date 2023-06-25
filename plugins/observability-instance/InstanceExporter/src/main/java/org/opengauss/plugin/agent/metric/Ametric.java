/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * used by {@code com.nctigba.server.HttpServer}
 */
public interface Ametric {
    /**
     * generate metric
     * 
     * @param dbPort TODO
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException;
}