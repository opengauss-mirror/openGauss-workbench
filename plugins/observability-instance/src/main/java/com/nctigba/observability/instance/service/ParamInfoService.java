package com.nctigba.observability.instance.service;

import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.model.param.ParamQuery;

import java.util.List;

public interface ParamInfoService {
    List<ParamInfoDTO> getParamInfo(ParamQuery paramQuery);
}
