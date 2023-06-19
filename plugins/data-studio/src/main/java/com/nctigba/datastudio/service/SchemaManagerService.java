/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.SchemaManagerRequest;

import java.util.List;
import java.util.Map;

public interface SchemaManagerService {
    List<String> queryAllUsers(SchemaManagerRequest request) throws Exception;

    Map<String, String> querySchema(SchemaManagerRequest request) throws Exception;

    void createSchema(SchemaManagerRequest request) throws Exception;

    void updateSchema(SchemaManagerRequest request) throws Exception;

    void deleteSchema(SchemaManagerRequest request) throws Exception;
}
