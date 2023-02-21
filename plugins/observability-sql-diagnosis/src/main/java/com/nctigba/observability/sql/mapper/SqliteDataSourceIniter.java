package com.nctigba.observability.sql.mapper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SqliteDataSourceIniter {
	@Value("${sqlitePath:data/diagnosis.db}")
	private String path;
	@Value("${sqliteinit:false}")
	private boolean refresh;
	boolean needInit = true;

	private String[] initSqls = {
			"CREATE TABLE \"diagnosis_task\" ( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,  clusterId TEXT,"
					+ " nodeId TEXT, dbName TEXT, sqlId TEXT, name TEXT, \"sql\" TEXT, conf TEXT,"
					+ " starttime DATETIME, endtime DATETIME, lasttime DATETIME, progress INTEGER,"
					+ " pid INTEGER, state TEXT, createtime DATETIME, tasktype TEXT, remarks TEXT, data TEXT);",
			"CREATE TABLE \"diagnosis_task_result\" ( taskid INTEGER, resultState TEXT, resultType TEXT,"
					+ " frameType TEXT, bearing TEXT, \"data\" TEXT);",
			"CREATE TABLE diagnosis_resource ( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, taskid INTEGER,"
					+ " grabType TEXT, f BLOB);",
			"CREATE TABLE dictionary_config (id TEXT NOT NULL PRIMARY KEY, nodeId TEXT, key TEXT, value TEXT);",
			"CREATE TABLE Threshold (knowledge_key TEXT NOT NULL PRIMARY KEY, v TEXT);" };

	@PostConstruct
	public void init() throws IOException {
		File f = new File(path);
		log.info("sqlite:" + f.getCanonicalPath());
		if (!f.exists()) {
			needInit = true;
			var parent = f.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
			f.createNewFile();
		} else if (refresh) {
			needInit = true;
			f.delete();
			f.createNewFile();
		}
		if (needInit) {
			var sqLiteDataSource = new SQLiteDataSource();
			sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath());
			try (var conn = sqLiteDataSource.getConnection();) {
				for (String sql : initSqls)
					try {
						conn.createStatement().execute(sql);
					} catch (SQLException e) {
					}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Bean
	public DynamicDataSourceProvider sqliteIniter() {
		return new DynamicDataSourceProvider() {
			@Override
			public Map<String, DataSource> loadDataSources() {
				File f = new File(path);
				var sqLiteDataSource = new SQLiteDataSource();
				try {
					sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath() + "?date_string_format=yyyy-MM-dd HH:mm:ss");
				} catch (IOException e) {
					throw new CustomException(e.getMessage());
				}
				var ds = new HikariDataSource();
				ds.setDataSource(sqLiteDataSource);
				ds.setMaximumPoolSize(1);
				return Map.of(Ds.diagnosis, ds);
			}
		};
	}
}