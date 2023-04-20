package org.GameBot.GameBot.DataBase;

import java.sql.*;

public class DataBase {
    private String _host;
    private String _password;
    private String _name;
    private String _user;
    public DataBase(String host, String password, String name, String user){
        _host = host;
        _password = password;
        _name = name;
        _user = user;
    }

    public ResultSet query() throws SQLException {
        Connection connection = DriverManager.getConnection(_host, _user, _password);
        Statement statement = connection.createStatement();
        var result = statement.executeQuery("SELECT  * ");
        connection.close();
        return result;
        /*Mysql connnect = mysql_connect(host, user, password, name);
        String result = mysqli_query(connnect, "SELECT * ");
        mysqli_close(connect)
        return reault*/
    }

}
