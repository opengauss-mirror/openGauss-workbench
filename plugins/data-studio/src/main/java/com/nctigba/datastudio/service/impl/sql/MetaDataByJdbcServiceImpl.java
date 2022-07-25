package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.util.ConnectionUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.sql.Connection;

/**
 * MetaDataByJdbcServiceImpl
 *
 * @author tang
 */
@Service
public class MetaDataByJdbcServiceImpl implements MetaDataByJdbcService {


    @Override
    public void testQuerySQL(String jdbcUrl, String userName, String password, String sql) {
        try (Connection connection = ConnectionUtils.connectGet(jdbcUrl, userName, password)) {
            connection.prepareStatement(sql);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }
}
