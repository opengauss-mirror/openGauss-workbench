/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  PageBaseQuery.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/query/PageBaseQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.query;

import com.nctigba.observability.sql.service.impl.BasePageDTO;
import com.nctigba.observability.sql.service.MyPage;

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