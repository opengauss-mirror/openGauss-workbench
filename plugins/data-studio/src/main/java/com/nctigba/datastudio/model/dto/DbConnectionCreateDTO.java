package com.nctigba.datastudio.model.dto;


import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DbConnectionCreateDTO {

    private String connectionid;

    private String id;

    private String type;
    private String name;

    private String driver;
    private String ip;
    private String port;
    private String dataName;
    private String userName;
    private String password;

    private String webUser;


    public DatabaseConnectionDO toDatabaseConnection() {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        databaseConnectionDO.setId(id);
        databaseConnectionDO.setType(type);
        databaseConnectionDO.setName(name.trim());
        databaseConnectionDO.setDriver("");
        databaseConnectionDO.setIp(ip);
        databaseConnectionDO.setPort(port);
        databaseConnectionDO.setDataName(dataName);
        databaseConnectionDO.setUserName(userName);
        databaseConnectionDO.setPassword(password);
        databaseConnectionDO.setWebUser(webUser);

        return databaseConnectionDO;
    }
}
