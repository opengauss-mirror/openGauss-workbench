package com.nctigba.observability.sql.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

public class TableFormatter {
	/**
	 * 
	 * @param file
	 * @param flag line first title
	 */
	public static tableData format(MultipartFile file, String flag) {
		tableData table = null;
		try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
			String[] keys = null;
			while (reader.ready()) {
				var line = reader.readLine();
				if (StringUtils.isBlank(line))
					continue;
				if (keys == null && line.startsWith(flag)) {
					keys = line.split("\\s+");
					table = new tableData(keys);
					continue;
				}
				if (table == null)
					continue;
				String[] datas = line.split("\\s+");
				if (datas.length < table.getColumns().size())
					break;
				var map = new HashMap<String, String>();
				if(keys!=null){
					for (int i = 0; i < table.getColumns().size(); i++){
						map.put(keys[i], datas[i]);
					}
				}
				table.addData(map);
			}
		} catch (IOException e) {
			throw new CustomException("table format err");
		}
		return table;
	}

	@Data
	@NoArgsConstructor
	public static class tableData {
		private List<Map<String, String>> columns = new ArrayList<>();
		private List<Map<String, String>> data = new ArrayList<>();

		public tableData(String[] keys) {
			for (String key : keys) {
				columns.add(Map.of("key", key, "title", key));
			}
		}

		public void addData(Map<String, String> map) {
			data.add(map);
		}
	}
}