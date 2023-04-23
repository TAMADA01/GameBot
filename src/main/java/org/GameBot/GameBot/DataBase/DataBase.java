package org.GameBot.GameBot.DataBase;

import lombok.SneakyThrows;
import org.GameBot.GameBot.Bot.BotConfig;
import org.GameBot.GameBot.PetFolder.Pet;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;
import java.util.List;

@Component
public class DataBase {

    private String _host;
    private String _password;
    private String _name;
    private String _user;
    BotConfig botConfig;
    Connection connection;

    @SneakyThrows
    public DataBase(BotConfig config){
        botConfig = config;

        _host = botConfig.get_host();
        _password = botConfig.get_password();
        _name = botConfig.get_name();
        _user = botConfig.get_user();

        connection = DriverManager.getConnection(_host);
    }

    public ResultSet query(String sqlQuery) {
        try {
            //Connection connection = DriverManager.getConnection(_host);
            Statement statement = connection.createStatement();
            return statement.executeQuery(sqlQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String sqlQuery){
        try{
            //Connection connection = DriverManager.getConnection(_host);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public String insertPet( String userID, String chatID, String name){
        return String.format("INSERT INTO pets (id, userID, chatID, name) VALUES (NULL, '%s', '%s', '%s')", userID, chatID, name);
    }

    public String insertMission(String chatID, String description, String reward){
        return String.format("INSERT INTO missions (id, chatID, description, reward) VALUES (NULL, '%s', '%s', '%s')", chatID, description, reward);
    }

    public String insertUsers(String chatID, String userID, String firstName, String lastName, String username){
        return String.format("INSERT INTO users (id, chatID, userID, firstName, lastName, username) VALUES (NULL, %s, '%s', '%s', '%s', '%s')", chatID, userID, firstName, lastName, username);
    }

    public String update(String table, String column, String newValue, String where){
        return String.format("UPDATE %s SET %s = '%s' WHERE %s", table, column, newValue, where);
    }

    public String delete(String table, String where){
        return String.format("DELETE FROM %s WHERE %s", table, where);
    }

    public boolean havePet(String chatID, String userID) {
        var sqlQuery = select("pets", "*", String.format("chatID = '%s' AND userID = '%s'", chatID, userID));
        var result = query(sqlQuery);
        try {
            return result.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public Pet getPet(String chatID, String userID) {
        var sqlQuery = select("pets", "*", String.format("chatID = '%s' AND userID = '%s'", chatID, userID));
        return new Pet(query(sqlQuery));
    }

    public void createPet(String userID, String chatID, String name) {
        var sqlQuery = insertPet(userID, chatID, name);
        update(sqlQuery);
    }

    public void renamePet(String userID, String chatID, String name) {
        var sqlQuery = update("pets", "name", name, String.format("userID = '%s' AND chatID = '%s'", userID, chatID));
        update(sqlQuery);
    }

    public ResultSet getMissionList(String chatID) {
        var sqlQuery = select("missions", "*", String.format("chatID = '%s'", chatID));
        return query(sqlQuery);
    }

    public void createMission(String chatID, String description, String reward) {
        var sqlQuery = insertMission(chatID, description, reward);
        update(sqlQuery);
    }

    public void setReward(String userID, String chatID, String money) {
        var sqlQuery = update("pets", "money", money, String.format("userID = '%s' AND chatID = '%s'", userID, chatID));
        update(sqlQuery);
    }

    @SneakyThrows
    public int getReward(String id) {
        var sqlQuery = select("missions", "*", String.format("id = '%s'", id));
        var result = query(sqlQuery);
        return result.getInt("reward");

    }

    @SneakyThrows
    public int getMoney(String userID, String chatID) {
        var sqlQuery = select("pets", "money", String.format("chatID = '%s' AND userID = '%s'", chatID, userID));
        var result = query(sqlQuery);
        return result.getInt("money");
    }

    public void addUser(String chatID, User user) {
        var sqlQuery = insertUsers(chatID, user.getId().toString(), user.getFirstName(), user.getLastName(), user.getUserName());
        update(sqlQuery);
    }

    @SneakyThrows
    public String getUserID(String username) {
        var sqlQuery = select("users", "userID", String.format("username = '%s'", username));
        var result = query(sqlQuery);
        return String.valueOf(result.getInt("userID"));
    }
    public ResultSet getUserList(String chatID) {
        var sqlQuery = select("users", "*", String.format("chatID = '%s'", chatID));
        return query(sqlQuery);
    }

    public void deleteMission(String id) {
        var sqlQuery = delete("missions", String.format("id = '%s'", id));
        update(sqlQuery);
    }

    public void addChat(String id) {
        var sqlQuery = insert("chats", "chatID", id);
        update(sqlQuery);
    }

    public void deleteChat(String id) {
        var sqlQuery = delete("chats", String.format("chatID = '%s'", id));
        update(sqlQuery);
    }

    public void addAdministrators(String chatID, String userID){
        var sqlQuery = insert("administrators", "chatID, userID", String.format("'%s','%s'", chatID, userID));
        update(sqlQuery);
    }

    public void deleteAdministrators(String chatId){
        var sqlQuery = delete("administrators", String.format("chatID = '%s'", chatId));
        update(sqlQuery);
    }

    @SneakyThrows
    public boolean isAdministrator(String chatID, String userId){
        var sqlQuery = select("administrators", "*", String.format("chatID = '%s' AND userID = '%s'", chatID, userId));
        var result = query(sqlQuery);
        return result.next();
    }
}
