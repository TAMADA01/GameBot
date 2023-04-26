package org.GameBot.GameBot.PetFolder;

import lombok.SneakyThrows;

import java.sql.ResultSet;

public class Inventory {
    public String userID;
    public String chatID;
    public int firstAidKit;
    public int energetics;
    public int powerBooster;

    @SneakyThrows
    public Inventory(ResultSet result) {
        userID = result.getString("userID");
        chatID = result.getString("chatID");
        firstAidKit = result.getInt("firstAidKit");
        energetics = result.getInt("energetics");
        powerBooster = result.getInt("powerBooster");
    }

    public int getItem(String column){
        if (column.equals("firstAidKit")){
            return firstAidKit;
        }else if (column.equals("energetics")){
            return energetics;
        }
        else if (column.equals("powerBooster")){
            return powerBooster;
        } else{
            return -1;
        }
    }
}
