package com.nctigba.datastudio.model.dto;

import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;

@NoArgsConstructor
@Data
public class ConnectionDTO {
    private Date lastDate;
    private HashSet socketSet = new HashSet<>();
    private String url;
    private String dbUser;
    private String dbPassword;


    public void setConnectionDTO(DatabaseConnectionUrlDO databaseConnectionUrlDO, String socketSet) {
        Date date = new Date();
        this.lastDate = date;
        this.socketSet.add(socketSet);
        this.url = databaseConnectionUrlDO.getUrl();
        this.dbUser = databaseConnectionUrlDO.getUserName();
        this.dbPassword = databaseConnectionUrlDO.getPassword();
    }

    public void updataConnectionDTO(ConnectionDTO aon, String socketSet) {
        Date date = new Date();
        this.lastDate = date;
        this.socketSet.add(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
    }

    public void updataConnectionDTO(ConnectionDTO aon) {
        Date date = new Date();
        this.lastDate = date;
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
    }

    public void reduceConnectionDTO(ConnectionDTO aon, String socketSet) {
        Date date = new Date();
        this.lastDate = date;
        this.socketSet.remove(socketSet);
        this.url = aon.getUrl();
        this.dbUser = aon.getDbUser();
        this.dbPassword = aon.getDbPassword();
    }
}
