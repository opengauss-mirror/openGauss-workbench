/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OpengaussEbpfApplication {


    public static void main(String[] args) {
        SpringApplication.run(OpengaussEbpfApplication.class, args);

    }

}
