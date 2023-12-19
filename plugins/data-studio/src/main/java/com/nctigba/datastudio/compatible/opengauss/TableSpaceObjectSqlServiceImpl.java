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
 *  TableSpaceObjectSqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/TableSpaceObjectSqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.TableSpaceObjectSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.RequestTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateTablespaceAttributeDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_MAXSIZE_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_NAME_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_OWNER_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_RANDOM_PAGE_COST_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_SEQ_PAGE_COST_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ATTRIBUTE_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LOCAL_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MAXSIZE_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.OWNER_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PATH_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RANDOM_PAGE_COST_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RELATIVE_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SEQ_PAGE_COST_TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.WITH_TABLESPACE_SQL;
import static com.nctigba.datastudio.utils.DebugUtils.cutBrace;

/**
 * UserObjectSQLServiceImpl achieve
 *
 * @since 2023-10-10
 */
@Slf4j
@Service
public class TableSpaceObjectSqlServiceImpl implements TableSpaceObjectSqlService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String createTablespaceDDL(DatabaseCreateTablespaceDTO request) {
        log.info("createTablespaceDDL request is {}:", request);
        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format(CREATE_TABLESPACE_SQL, DebugUtils.needQuoteName(request.getTablespaceName())));
        if (StringUtils.isNotEmpty(request.getOwner())) {
            String cowner = String.format(OWNER_TABLESPACE_SQL, request.getOwner());
            ddl.append(cowner);
        }
        log.info("request.isRelativePath() {}:", request.isRelativePath());
        if (request.isRelativePath()) {
            ddl.append(RELATIVE_TABLESPACE_SQL);
        }
        if (StringUtils.isNotEmpty(request.getPath())) {
            ddl.append(String.format(LOCAL_TABLESPACE_SQL, request.getPath()));
        } else {
            throw new CustomException(LocaleStringUtils.transLanguage("2022"));
        }
        if (StringUtils.isNotEmpty(request.getMaxStorage())) {
            String maxsize = String.format(MAXSIZE_TABLESPACE_SQL, request.getMaxStorage());
            ddl.append(maxsize);
        }
        StringBuilder withDdl = new StringBuilder();
        if (StringUtils.isNotEmpty(request.getSequentialOverhead())) {
            String sequential = String.format(SEQ_PAGE_COST_TABLESPACE_SQL, request.getSequentialOverhead());
            withDdl.append(sequential).append(COMMA);
        }
        if (StringUtils.isNotEmpty(request.getNonSequentialOverhead())) {
            String nonSequential = String.format(RANDOM_PAGE_COST_TABLESPACE_SQL, request.getNonSequentialOverhead());
            withDdl.append(nonSequential).append(COMMA);
        }
        if (StringUtils.isNotEmpty(withDdl)) {
            String withOther = String.format(WITH_TABLESPACE_SQL, withDdl.deleteCharAt(withDdl.length() - 1));
            ddl.append(withOther);
        }
        ddl.append(SEMICOLON);
        if (StringUtils.isNotEmpty(request.getComment())) {
            String comment = String.format(COMMENT_TABLESPACE_SQL,
                    DebugUtils.needQuoteName(request.getTablespaceName()), request.getComment());
            ddl.append(comment);
        }
        log.info("createTablespaceDDL :{}", ddl);
        return String.valueOf(ddl);
    }

    @Override
    public String dropTablespaceDDL(String name) {
        return String.format(DROP_TABLESPACE_SQL, DebugUtils.needQuoteName(name));
    }

    @Override
    public DatabaseTablespaceAttributeDTO tablespaceAttribute(RequestTablespaceAttributeDTO request) {
        log.info("tablespaceAttribute request is :{}", request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                Statement statementPath = connection.createStatement()
        ) {
            DatabaseTablespaceAttributeDTO tablespaceAttributeDTO = new DatabaseTablespaceAttributeDTO();
            ResultSet resultSet = statement.executeQuery(String.format(ATTRIBUTE_TABLESPACE_SQL, request.getOid()));
            while (resultSet.next()) {
                List<String> role = cutBrace(resultSet.getString("spcoptions"));
                for (String str : role) {
                    if ("seq_page_cost".equals(StringUtils.substringBefore(str, "="))) {
                        tablespaceAttributeDTO.setSequentialOverhead(StringUtils.substringAfter(str, "="));
                    } else {
                        tablespaceAttributeDTO.setNonSequentialOverhead(StringUtils.substringAfter(str, "="));
                    }
                }
                tablespaceAttributeDTO.setTablespaceName(resultSet.getString("spcname"));
                tablespaceAttributeDTO.setMaxStorage(resultSet.getString("spcmaxsize"));
                tablespaceAttributeDTO.setComment(resultSet.getString("Description"));
                tablespaceAttributeDTO.setOwner(resultSet.getString("rolname"));
                tablespaceAttributeDTO.setRelativePath(resultSet.getBoolean("relative"));
            }

            ResultSet resultSetPath = statementPath.executeQuery(String.format(PATH_TABLESPACE_SQL, request.getOid()));
            while (resultSetPath.next()) {
                tablespaceAttributeDTO.setPath(resultSetPath.getString(1));
            }
            log.info("tablespaceAttributeDTO :{}", tablespaceAttributeDTO);
            return tablespaceAttributeDTO;
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public String tablespaceUpdateAttributeDdl(UpdateTablespaceAttributeDTO request) {
        log.info("tablespaceUpdateAttributeDdl request {}", request);
        StringBuilder ddl = new StringBuilder();
        String tablespaceName;
        if (StringUtils.isNotEmpty(request.getNewTablespaceName())) {
            tablespaceName = DebugUtils.needQuoteName(request.getNewTablespaceName());
            if (!request.getNewTablespaceName().equals(request.getOldTablespaceName())) {
                ddl.append(String.format(ALTER_NAME_TABLESPACE_SQL,
                        DebugUtils.needQuoteName(request.getOldTablespaceName()),
                        DebugUtils.needQuoteName(request.getNewTablespaceName())));
            }
        } else {
            tablespaceName = DebugUtils.needQuoteName(request.getOldTablespaceName());
        }
        if (StringUtils.isNotEmpty(request.getOwner())) {
            String cowner = String.format(ALTER_OWNER_TABLESPACE_SQL, tablespaceName, request.getOwner());
            ddl.append(cowner);
        }
        if (StringUtils.isNotEmpty(request.getMaxStorage())) {
            String maxsize = String.format(ALTER_MAXSIZE_TABLESPACE_SQL, tablespaceName, request.getMaxStorage());
            ddl.append(maxsize);
        }
        if (StringUtils.isNotEmpty(request.getSequentialOverhead())) {
            String nonSequential = String.format(ALTER_SEQ_PAGE_COST_SQL, tablespaceName,
                    "seq_page_cost", request.getSequentialOverhead());
            ddl.append(nonSequential);
        }
        if (StringUtils.isNotEmpty(request.getNonSequentialOverhead())) {
            String nonSequential = String.format(ALTER_RANDOM_PAGE_COST_SQL, tablespaceName,
                    "random_page_cost", request.getNonSequentialOverhead());
            ddl.append(nonSequential);
        }
        ddl.append(String.format(COMMENT_TABLESPACE_SQL, tablespaceName, request.getComment()));
        log.info("tablespaceUpdateAttributeDdl {}", ddl);
        return String.valueOf(ddl);
    }
}
