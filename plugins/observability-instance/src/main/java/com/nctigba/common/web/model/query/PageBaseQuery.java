package com.nctigba.common.web.model.query;

import com.nctigba.common.mybatis.BasePageDTO;
import com.nctigba.common.mybatis.MyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PageBaseQuery {
	@ApiModelProperty(value = "Page number", required = true)
	private int pageNum;
	@ApiModelProperty(value = "Page size", required = true)
	private int pageSize;
	@ApiModelProperty(value = "query total record count or not")
	private Boolean queryCount = false;

	@ApiModelProperty(hidden = true)
	public <T> MyPage<T> iPage() {
		return new BasePageDTO<T>(pageNum, pageSize, queryCount);
	}

}