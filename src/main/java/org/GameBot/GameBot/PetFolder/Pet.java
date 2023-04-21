package org.GameBot.GameBot.PetFolder;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Pet {
    public String userID;
    public String chatID;
    public String name;
    public int health;
    public int hunger;
    public int power;
    public String clanID;

    public Pet(){

    }

    public Pet(ResultSet result) throws SQLException {
        userID = result.getString("userID");
        chatID = result.getString("chatID");
        name = result.getString("name");
        health = result.getInt("health");
        hunger = result.getInt("hunger");
        power = result.getInt("power");
        clanID = result.getString("clanID");
    }
}
