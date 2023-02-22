package com.nctigba.observability.instance.service;

import com.nctigba.observability.instance.dto.param.DatabaseParamDTO;
import com.nctigba.observability.instance.dto.param.OsParamDTO;
import com.nctigba.observability.instance.model.param.ParamQuery;

import java.util.List;

public interface ParamInfoService {
    List<DatabaseParamDTO> getDatabaseParamInfo(ParamQuery paramQuery);
    List<OsParamDTO> getOsParamInfo(ParamQuery paramQuery);
}
