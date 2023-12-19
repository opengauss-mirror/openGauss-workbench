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
 *  LineChartVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/LineChartVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LineChartVO {
	private String title;
	private xAxis xAxis = new xAxis();
	private List<line> series = new ArrayList<>();

	public LineChartVO addX(String s) {
		xAxis.getData().add(s);
		return this;
	}

	public LineChartVO delLine(String id) {
		var it = series.iterator();
		while (it.hasNext())
			if (it.next().getId().equals(id)) {
				it.remove();
				break;
			}
		return this;
	}

	public <T extends Number> LineChartVO addPoint(String name, T data) {
		return addPoint(name, name, data);
	}

	public <T extends Number> LineChartVO addPoint(String id, String name, T data) {
		int xsize = xAxis.getData().size();
		if(xsize == 0)
			return this;
		for (line line : series)
			if (line.getId().equals(id)) {
				var d = line.getData();
				while (d.size() < xsize - 1)
					d.add(0);
				if (d.size() < xsize)
					d.add(data);
				else
					d.set(xsize - 1, NumberUtil.add(d.get(xsize - 1), data));
				return this;
			}
		List<Number> list = new ArrayList<>();
		for (int i = 0; i < xsize - 1; i++)
			list.add(0);
		list.add(data);
		series.add(new line().setId(id).setName(name).setData(list));
		return this;
	}

	@Data
	public class xAxis {
		private Set<String> data = new LinkedHashSet<>();
	}

	@Data
	@Accessors(chain = true)
	public class line {
		private String id;
		private String name;
		private List<Number> data = new ArrayList<>();
	}

	/**
	 * Fill in the missing 0
	 */
	public void parse() {
		int xsize = xAxis.getData().size();
		series.forEach(line -> {
			var data = line.getData();
			while (data.size() < xsize) {
				data.add(0);
			}
		});
	}
}