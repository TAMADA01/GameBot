package org.GameBot.GameBot.DataBase;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
@Configuration
@Data
@PropertySource("application.properties")
public class DataBase {
    @Value("${db.host}")
    private String _host;
    @Value("${db.password}")
    private String _password;
    @Value("${db.name}")
    private String _name;
    @Value("${db.user}")
    private String _user;

    /*public DataBase(String host, String password, String name, String user){
        _host = host;
        _password = password;
        _name = name;
        _user = user;
    }*/

    public ResultSet query() throws SQLException {
        Connection connection = DriverManager.getConnection(_host);
        Statement statement = connection.createStatement();
        var result = statement.executeQuery("SELECT * FROM pets");
        connection.close();
        return result;
        /*Mysql connnect = mysql_connect(host, user, password, name);
        String result = mysqli_query(connnect, "SELECT * ");
        mysqli_close(connect)
        return reault*/
    }

}
