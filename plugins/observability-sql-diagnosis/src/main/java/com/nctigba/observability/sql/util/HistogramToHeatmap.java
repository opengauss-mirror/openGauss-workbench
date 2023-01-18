package com.nctigba.observability.sql.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.util.ReUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HistogramToHeatmap
 * 
 * @formatter:off
Sampling run queue length... Hit Ctrl-C to end.

12:16:18
     runqlen       : count     distribution
        0          : 125      |*************************               |
        1          : 200      |****************************************|
        2          : 153      |******************************          |
        3          : 137      |***************************             |
        4          : 93       |******************                      |
        5          : 38       |*******                                 |
@formatter:on
 */
public class HistogramToHeatmap {
	private static final String timeRegex = "([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]):?";
	private static final String flagRegex = "[A-Za-z]+ = (.*)";
	private static final String dataRegex = "^\\s*(\\d*|\\d*\\s*->\\s*\\d*)\\s*:\\s*([0-9]*)\\s*\\|";

	public static List<HeatMap> format(MultipartFile file, String unit) {
		try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
			var datas = new ArrayList<ArrayList<ArrayList<Integer>>>();
			var xData = new ArrayList<String>();
			var yData = new ArrayList<String>();
			var flags = new ArrayList<String>();
			var flag = "";
			while (reader.ready()) {
				var line = reader.readLine();
				if (StringUtils.isBlank(line))
					continue;
				if (ReUtil.isMatch(timeRegex, line)) {
					xData.add(line.trim());
					for (ArrayList<ArrayList<Integer>> data : datas)
						data.add(new ArrayList<>());
					continue;
				}
				var f = ReUtil.getGroup1(flagRegex, line);
				if (StringUtils.isNotBlank(f)) {
					flag = f;
					if (flags.contains(flag))
						continue;
					ArrayList<ArrayList<Integer>> e = new ArrayList<>();
					if (datas.size() > 0)
						for (int i = 0; i < datas.get(0).size(); i++)
							e.add(new ArrayList<>());
					datas.add(e);
					flags.add(flag);
					continue;
				}
				var y = ReUtil.getGroup1(dataRegex, line);
				if (StringUtils.isBlank(y))
					continue;
				if (!yData.contains(y))
					yData.add(y);
				var count = ReUtil.get(dataRegex, line, 2);
				if (flags.indexOf(flag) < 0) {
					flags.add("default");
					flag = "default";
					datas.add(new ArrayList<>());
				} else if (datas.size() <= flags.indexOf(flag))
					datas.add(new ArrayList<>());
				var m1 = datas.get(flags.indexOf(flag));
				if (m1.size() == 0)
					m1.add(new ArrayList<>());
				m1.get(m1.size() - 1).add(Integer.valueOf(count));
			}
			return HeatMap.toHeatMap(flags, xData, yData, datas, unit);
		} catch (NumberFormatException | IOException e) {
			throw new CustomException("heatmap format err ", e);
		} finally {
		}
	}

	@Data
	@NoArgsConstructor
	public static class HeatMap {
		String name;
		String unit;
		List<String> x;
		List<String> y;
		// {0,2,3} => left bottom (0,0)
		// {2,1,5}
		// * 5 *
		// * * *
		// * * 3
		// List<rowNum,<lineNum，data>>
		List<List<Integer>> data;
		List<ArrayList<Integer>> source;

		public HeatMap(String name, List<String> x, List<String> y, ArrayList<ArrayList<Integer>> source, String unit) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.source = source;
			this.unit = unit;
			this.data = new ArrayList<>();
			Integer t;
			for (int i = 0; i < source.size(); i++)
				for (int j = 0; j < source.get(i).size(); j++)
					if ((t = source.get(i).get(j)) != 0)
						this.data.add(Arrays.asList(i, j, t));
		}

		public static List<HeatMap> toHeatMap(ArrayList<String> names, ArrayList<String> x, ArrayList<String> y,
				ArrayList<ArrayList<ArrayList<Integer>>> datas, String unit) {
			List<HeatMap> list = new ArrayList<>();
			for (int i = 0; i < names.size(); i++)
				list.add(new HeatMap(names.get(i), x, y, datas.get(i), unit));
			return list;
		}
	}
}