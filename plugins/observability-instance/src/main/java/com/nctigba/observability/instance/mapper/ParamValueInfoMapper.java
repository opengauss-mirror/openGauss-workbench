/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nctigba.observability.instance.config.ParamInfoInitConfig;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.dto.param.ParamInfoDTO;
import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.entity.ParamValueInfo;

import cn.hutool.core.util.StrUtil;

@Service
public class ParamValueInfoMapper {
    private static final String SELECT = "select * from param_value_info where instance = ?";
    private static final String DELETE = "delete from param_value_info where sid in (?)";
    private static final String INSERT = "insert into param_value_info(sid,instance,actualValue) values(?,?,?)";

    public static List<ParamValueInfo> selectByInstanceId(String instanceId) {
        try (var stmt = conn().prepareStatement(SELECT);) {
            stmt.setString(1, instanceId);
            try (var rs = stmt.executeQuery();) {
                return ParamValueInfo.parse(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(CommonConstants.SQLITE_ERROR, e);
        }
    }

    public static void insert(ParamValueInfo paramValueInfo) {
        try (var pstmt = conn().prepareStatement(INSERT);) {
            pstmt.setInt(1, paramValueInfo.getSid());
            pstmt.setString(2, paramValueInfo.getInstance());
            pstmt.setString(3, paramValueInfo.getActualValue());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(CommonConstants.SQLITE_ERROR, e);
        }
    }

    public static void insertBatch(List<ParamValueInfo> paramValueInfos) {
        try (var pstmt = conn().prepareStatement(INSERT);) {
            for (ParamValueInfo paramValueInfo : paramValueInfos) {
                pstmt.setInt(1, paramValueInfo.getSid());
                pstmt.setString(2, paramValueInfo.getInstance());
                pstmt.setString(3, paramValueInfo.getActualValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(CommonConstants.SQLITE_ERROR, e);
        }
    }

    public static void delBySids(List<Integer> ids) {
        if (ids.isEmpty())
            return;
        var idstr = StrUtil.join(",", ids.toArray());
        try (var stmt = conn().createStatement();) {
            stmt.executeUpdate(DELETE.replace("?", idstr));
        } catch (SQLException e) {
            throw new RuntimeException(CommonConstants.SQLITE_ERROR, e);
        }
    }

    @Cacheable(cacheNames = "paraminfo", key = "#nodeId")
    public List<ParamInfoDTO> query(String nodeId) {
        var result = new ArrayList<ParamInfoDTO>();
        var list = ParamValueInfoMapper.selectByInstanceId(nodeId);
        var map = new HashMap<Integer, ParamValueInfo>();
        for (ParamValueInfo paramValueInfo : list)
            map.put(paramValueInfo.getSid(), paramValueInfo);
        for (ParamInfo info : ParamInfoMapper.getAll()) {
            String act;
            if (map.containsKey(info.getId()))
                act = map.get(info.getId()).getActualValue();
            else
                act = "";
            result.add(new ParamInfoDTO(info, act));
        }
        return result;
    }

    @CacheEvict(cacheNames = "paraminfo", key = "#nodeId")
    public void refresh(String nodeId) {
        // Do nothing because of X and Y.
    }

    private static final Connection conn() {
        return ParamInfoInitConfig.getCon(ParamInfoInitConfig.PARAMVALUEINFO);
    }
}