package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;

public interface DatabaseFunctionSPService {

    void dropFunctionSP(DatabaseFunctionSPDTO request) throws Exception;
}
