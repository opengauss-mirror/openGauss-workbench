package com.nctigba.observability.instance.dto.param;

import com.nctigba.observability.instance.entity.ParamInfo;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParamInfoDTO {
	private Integer id;
	private String paramType;
	private String paramName;
	private String paramDetail;
	private String actualValue;
	private String suggestValue;
	private String defaultValue;
	private String unit;
	private String suggestExplain;

	public ParamInfoDTO(ParamInfo info, String actualValue) {
		BeanUtil.copyProperties(info, this);
		this.actualValue = actualValue;
	}
}