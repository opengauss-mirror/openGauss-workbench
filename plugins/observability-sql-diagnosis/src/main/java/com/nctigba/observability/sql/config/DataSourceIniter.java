package com.nctigba.observability.sql.config;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@EnableScheduling
public class DataSourceIniter {
	public static final String diagnosis = "diagnosis";
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
	public static final String primary = "primary";

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
				return Map.of(diagnosis, sqLiteDataSource, primary, dataSource());
			}
		};
	}

	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
		// read config from dataKit platform
		String url = environmentProvider.getString("spring.datasource.url");
		String username = environmentProvider.getString("spring.datasource.username");
		String password = environmentProvider.getString("spring.datasource.password");
		String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");

		return DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username)
				.password(password).build();
	}
}