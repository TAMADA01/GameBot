package org.GameBot.GameBot.DataBase;

import org.GameBot.GameBot.Bot.BotConfig;
import org.GameBot.GameBot.Main;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class DataBase {

    private String _host = "jdbc:sqlite:src/main/java/org/GameBot/GameBot/DataBase/DataBaseBot.db";
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

    public void query(String sqlQuery) throws SQLException {
        Connection connection = DriverManager.getConnection(_host);
        Statement statement = connection.createStatement();
        result = statement.executeQuery(sqlQuery);
    }

    public String select(String columns, String table, String where){
        return String.format("SELECT %s FROM %s %s", columns, table, where);
    }

    public String insert(String table, String columns, String values){
        return String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, values);
    }

    public String update(String table, String column, String newValue, String where){
        return String.format("UPDATE %s Set %s = '%s' WHERE %s", table, column, newValue, where);
    }

    public String delete(String table, String where){
        return String.format("DELETE FROM %s WHERE %s", table, where);
    }
}
