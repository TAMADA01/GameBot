package org.GameBot.GameBot.PetFolder;

import lombok.SneakyThrows;

import java.sql.ResultSet;

public class Pet {
    //Время в минутах
    public static final int WORKING_TIME = 240;
    public String userID;
    public String chatID;
    public String name;
    public String status;
    public long time;
    private int health;
    private int cheerfulness;
    private int hunger;
    private int power;
    private int money;
    public String clanID;

    public Pet(){

    }

    @SneakyThrows
    public Pet(ResultSet result) {
        userID = String.valueOf(result.getInt("userID"));
        chatID = String.valueOf(result.getInt("chatID"));
        name = result.getString("name");
        status = result.getString("status");
        time = result.getInt("time");
        health = result.getInt("health");
        cheerfulness = result.getInt("cheerfulness");
        hunger = result.getInt("hunger");
        power = result.getInt("power");
        money = result.getInt("money");
        clanID = result.getString("clanID");
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health > 100){
            this.health = 100;
        } else if (health < 0) {
            this.health = 0;
        }
        else {
            this.health = health;
        }
    }

    public int getCheerfulness() {
        return cheerfulness;
    }

    public void setCheerfulness(int cheerfulness) {
        if (cheerfulness > 100){
            this.cheerfulness = 100;
        } else if (cheerfulness < 0) {
            this.cheerfulness = 0;
        }
        else {
            this.cheerfulness = cheerfulness;
        }
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        if (hunger > 100){
            this.hunger = 100;
        } else if (hunger < 0) {
            this.hunger = 0;
        }
        else {
            this.hunger = hunger;
        }
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = Math.max(power, 0);
    }
}
