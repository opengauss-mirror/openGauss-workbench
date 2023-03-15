package com.nctigba.observability.instance.mapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.nctigba.observability.instance.config.ParamInfoInitConfig;
import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.entity.ParamInfo.type;

@Service
public class ParamInfoMapper {
	private static final String SQL = "select * from param_info";
	private static final List<ParamInfo> LIST = new ArrayList<>();
	private static final Map<Integer, ParamInfo> IDS = new HashMap<>();
	private static final Map<ParamInfo.type, Map<String, ParamInfo>> MAP = new HashMap<>();

	@PostConstruct
	public void refresh() {
		var connect = ParamInfoInitConfig.getCon(ParamInfoInitConfig.PARAMINFO);
		try {
			try (var stmt = connect.createStatement(); var rs = stmt.executeQuery(SQL);) {
				var list = ParamInfo.parse(rs);
				synchronized (LIST) {
					LIST.clear();
					LIST.addAll(list);
				}
				synchronized (IDS) {
					IDS.clear();
					for (ParamInfo paramInfo : list)
						IDS.put(paramInfo.getId(), paramInfo);
				}
				synchronized (MAP) {
					for (type v : ParamInfo.type.values())
						MAP.put(v, new HashMap<>());
					for (ParamInfo paramInfo : list)
						MAP.get(paramInfo.getParamType()).put(paramInfo.getParamName(), paramInfo);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("sqlite error", e);
		}
	}

	public static ParamInfo getById(Integer id) {
		return IDS.get(id);
	}

	public static List<ParamInfo> getAll() {
		return LIST;
	}

	public static ParamInfo getParamInfo(ParamInfo.type type, String name) {
		
		return MAP.get(type).get(name);
	}

	public static List<Integer> getIds(type t) {
		return MAP.get(t).values().stream().map(ParamInfo::getId).collect(Collectors.toList());
	}
}