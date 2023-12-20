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
 *  package-info.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/config/package-info.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.config;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateFormatter{
	static SimpleDateFormat getDateFormatter(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") {
			private static final long serialVersionUID = 1L;

			@Override
			public Date parse(String text, ParsePosition pos) {
				if(org.apache.commons.lang3.StringUtils.isBlank(text))
					return null;
				return super.parse(text, pos);
			}
		};
		sdf.setTimeZone(java.util.TimeZone.getTimeZone(java.time.ZoneOffset.UTC));
		return sdf;
	}
}