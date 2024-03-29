/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * http util
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Slf4j
public class HTTPUtil {


    /**
     * http httpUrlPost
     *
     * @param url file taskid type send ebpf data
     */
    public void httpUrlPost(String url, FileSystemResource file, String type) {
        final RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("type", type);
        map.add("file", file);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        try {
            log.info(type + ":" + response.getStatusCode() + ":" + response.getBody() + ":" + file.contentLength());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    /**
     * http httpUrlPost
     *
     * @param url data type
     */
    public void httpsSendDate(String url, String data, String type) {
        final RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("type", type);
        map.add("data", data);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        log.info(response.getStatusCode() + ":" + response.getBody() + ":" + data);
    }

}

