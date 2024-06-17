/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * HealthCheckController.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/controller/HealthCheckController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller;

import org.opengauss.admin.plugin.annotation.RateLimit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @date 2024/6/12 9:25
 * @since 0.0
 */
@RestController
@RequestMapping("/oauth")
public class HealthCheckController {

    /**
     * used to check the plugin status
     *
     * @return ResponseEntity
     */
    @RateLimit
    @RequestMapping(value = "/health", method = RequestMethod.HEAD)
    public ResponseEntity<?> healthCheck() {
        // Returns a response header indicating that the service is alive.
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
