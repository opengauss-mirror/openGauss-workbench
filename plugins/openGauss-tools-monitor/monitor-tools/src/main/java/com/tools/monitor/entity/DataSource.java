package com.tools.monitor.entity;

import lombok.Data;

/**
 * DataSource
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class DataSource {
    String id;
    String name;
    String url;
    String username;
    String password;
    String driver;
}
