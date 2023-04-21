package org.GameBot.GameBot.DataBase;

import org.GameBot.GameBot.Bot.BotConfig;
import org.GameBot.GameBot.PetFolder.Pet;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class DataBase {

    private String _host;
    private String _password;
    private String _name;
    private String _user;
    BotConfig botConfig;

    public DataBase(BotConfig config){
        botConfig = config;

        _host = botConfig.get_host();
        _password = botConfig.get_password();
        _name = botConfig.get_name();
        _user = botConfig.get_user();
    }

    public ResultSet query(String sqlQuery) throws SQLException {
        Connection connection = DriverManager.getConnection(_host);
        Statement statement = connection.createStatement();
        return statement.executeQuery(sqlQuery);
    }

    public int update(String sqlQuery) throws SQLException {
        Connection connection = DriverManager.getConnection(_host);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sqlQuery);
    }

    public String select(String table, String columns, String where){
        return String.format("SELECT %s FROM %s WHERE %s", columns, table, where);
    }

    public String select(String table, String columns){
        return String.format("SELECT %s FROM %s", columns, table);
    }

    public String selectCount(String table, String column){
        return String.format("SELECT COUNT(%s) FROM %s", column, table);
    }

    public String insert(String table, String columns, String values){
        return String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, values);
    }

    public String insertPet(String table, String userID, String chatID, String name){
        return String.format("INSERT INTO %s (id, userID, chatID, name) VALUES (NULL, '%s', '%s', '%s')", table, userID, chatID, name);
    }

    public String update(String table, String column, String newValue, String where){
        return String.format("UPDATE %s Set %s = '%s' WHERE %s", table, column, newValue, where);
    }

    public String delete(String table, String where){
        return String.format("DELETE FROM %s WHERE %s", table, where);
    }

    public boolean havePet(String chatID, String userID) throws SQLException {
        var sqlQuery = select("pets", "*", String.format("chatID = '%s' AND userID = '%s'", chatID, userID));
        var result = query(sqlQuery);
        return result.next();
    }

    public Pet getPet(String chatID, String userID) throws SQLException {
        var sqlQuery = select("pets", "*", String.format("chatID = '%s' AND userID = '%s'", chatID, userID));
        return new Pet(query(sqlQuery));
    }

    public void createPet(String userID, String chatID, String name) throws SQLException {
        var sqlQuery = insertPet("pets", userID, chatID, name);
        update(sqlQuery);
    }

    public void renamePet(String userID, String chatID, String name) throws SQLException {
        var sqlQuery = update("pets", "name", name, String.format("userID = '%s' AND chatID = '%s'", userID, chatID));
        update(sqlQuery);
    }
}
