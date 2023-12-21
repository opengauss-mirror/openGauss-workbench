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
 *  FastjsonConfig.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/config/FastjsonConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.opengauss.jdbc.PgArray;
import org.opengauss.util.PGobject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * FastjsonConfig
 *
 * @since 2023-06-25
 */
@Configuration
public class FastjsonConfig {
    /**
     * database other to json
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper fastJsonHttpMessageConverters() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(PgArray.class, new JsonSerializer<>() {
            @Override
            public void serialize(PgArray value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    if (null instanceof String) {
                        gen.writeString((String) null);
                    }
                    return;
                }
                gen.writeString(value.toString());
            }
        });
        module.addSerializer(PGobject.class, new JsonSerializer<>() {
            @Override
            public void serialize(
                    PGobject value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                if (value == null) {
                    if (null instanceof String) {
                        gen.writeString((String) null);
                    }
                    return;
                }
                gen.writeString(value.toString());
            }
        });
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
