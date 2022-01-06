
package com.nctigba.common.web.config;

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