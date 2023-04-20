package org.GameBot.GameBot.DataBase;

import org.GameBot.GameBot.Bot.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class DataBase {

    private String _host;
    private String _password;
    private String _name;
    private String _user;
    BotConfig botConfig;
    public ResultSet result;

    public DataBase(BotConfig config){
        botConfig = config;

        _host = botConfig.get_host();
        _password = botConfig.get_password();
        _name = botConfig.get_name();
        _user = botConfig.get_user();
    }

    /*public DataBase(String host, String password, String name, String user){
        _host = host;
        _password = password;
        _name = name;
        _user = user;
    }*/

    public void query() throws SQLException {
        Connection connection = DriverManager.getConnection(_host);
        Statement statement = connection.createStatement();
        result = statement.executeQuery("SELECT * FROM users");
        //connection.close();
        //return result;
        /*Mysql connnect = mysql_connect(host, user, password, name);
        String result = mysqli_query(connnect, "SELECT * ");
        mysqli_close(connect)
        return reault*/
    }

}
