package org.GameBot.GameBot.PetFolder;

import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Pet {
    public String userID;
    public String chatID;
    public String name;
    public int health;
    public int cheerfulness;
    public int hunger;
    public int power;
    public int money;
    public String clanID;

    public Pet(){

    }

    @SneakyThrows
    public Pet(ResultSet result) {
        userID = result.getString("userID");
        chatID = result.getString("chatID");
        name = result.getString("name");
        health = result.getInt("health");
        cheerfulness = result.getInt("cheerfulness");
        hunger = result.getInt("hunger");
        power = result.getInt("power");
        money = result.getInt("money");
        clanID = result.getString("clanID");
    }
}
