/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

@Configuration
public class FastjsonConfig {
    @Bean
    public ObjectMapper fastJsonHttpMessageConverters() {
        var objectMapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addSerializer(PgArray.class, new JsonSerializer<PgArray>() {
            @Override
            public void serialize(PgArray value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeString((String) null);
                    return;
                }
                gen.writeString(value.toString());
            }
        });
        module.addSerializer(PGobject.class, new JsonSerializer<PGobject>() {
            @Override
            public void serialize(PGobject value, JsonGenerator gen,
                                  SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeString((String) null);
                    return;
                }
                gen.writeString(value.toString());
            }
        });
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
