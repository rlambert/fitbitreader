package com.proclub.datareader.dao;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper {

    /*
    [UserGuid] [uniqueidentifier] NOT NULL,
[ModifiedDateTime] [int] NOT NULL,
[ClientType] [int] NOT NULL,
[Email] [varchar](100) NULL,
[fkUserStore2020] [varchar](50) NULL,
[fkUserStorePRO] [varchar](50) NULL,
[PostalCode] [nvarchar](12) NULL,
[fkClientId] [varchar](200) NULL,
 */

    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserGuid(rs.getString("UserGuid"));
        user.setModifiedDateTime(rs.getInt("ModifiedDateTime"));
        user.setClientType(rs.getInt("ClientType"));
        user.setEmail(rs.getString("Email"));
        user.setFkUserStore2020(rs.getString("fkUserStore2020"));
        user.setFkUserStorePRO(rs.getString("fkUserStorePRO"));
        user.setPostalCode(rs.getString("PostalCode"));
        user.setFkClientId(rs.getString("fkClientId"));
        return user;
    }
}