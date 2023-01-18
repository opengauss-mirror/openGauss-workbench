package com.nctigba.observability.sql.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DbMapper {
    @Select("SELECT datname FROM pg_database")
    List<String> dataBaseList();
}