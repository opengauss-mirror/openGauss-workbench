package com.nctigba.common.web.model.query;

import com.nctigba.common.mybatis.BasePageDTO;
import com.nctigba.common.mybatis.MyPage;

import lombok.Data;

@Data
public class PageBaseQuery {
	private int pageNum;
	private int pageSize;
	private Boolean queryCount = false;

	public <T> MyPage<T> iPage() {
		return new BasePageDTO<T>(pageNum, pageSize, queryCount);
	}

}