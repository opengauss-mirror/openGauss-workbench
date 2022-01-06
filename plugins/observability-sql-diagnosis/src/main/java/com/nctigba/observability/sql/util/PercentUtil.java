package com.nctigba.observability.sql.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.lang3.math.NumberUtils;

public class PercentUtil {
	public static double parse(String str, double def) {
		try {
			return NumberFormat.getPercentInstance().parse(str).doubleValue();
		} catch (ParseException e) {
			return def;
		}
	}

	public static final BigDecimal parse(String str) {
		return NumberUtils.toScaledBigDecimal(str).divide(new BigDecimal(100));
	}
}