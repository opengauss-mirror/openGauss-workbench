package com.nctigba.observability.instance.dto.param;

import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.util.MessageSourceUtil;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParamInfoDTO {
	private Integer id;
	private String paramType;
	private String paramName;
	private String actualValue;
	private String suggestValue;
	private String defaultValue;

	public ParamInfoDTO(ParamInfo info, String actualValue) {
		BeanUtil.copyProperties(info, this);
		this.actualValue = actualValue;
	}

	public String getParamDetail() {
		return MessageSourceUtil.get(paramType + "." + paramName + ".paramDetail");
	}

	public String getUnit() {
		return MessageSourceUtil.get(paramType + "." + paramName + ".unit");
	}

	public String getSuggestExplain() {
		return MessageSourceUtil.get(paramType + "." + paramName + ".suggestExplain");
	}
}