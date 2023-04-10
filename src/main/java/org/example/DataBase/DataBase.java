package org.example.DataBase;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Map;

public class DataBase {
    private String DATABASE_HOST = "";
    private String DATABASE_NAME = "";
    private String DATABASE_USER = "";
    private String DATABASE_PASSWORD = "";

    public DataBase(String host, String name, String user, String password){
        DATABASE_HOST = host;
        DATABASE_NAME = name;
        DATABASE_USER = user;
        DATABASE_PASSWORD = password;
    }

    public String insert(String table, Map<String, String> values){
        ArrayList<String> values_array = new ArrayList<>();
        ArrayList<String> fields_array = new ArrayList<>();

        for (var field : values.keySet()) {
            values_array.add("'" + values.get(field) + "'");
        }
        
        String values_string = String.join(",", values_array);
        String fields_string = String.join(",", fields_array);

        return String.format("INSERT INTO `" + table + "`(" + fields_string + ") VALUES (" + values_string + ")");
    }
}
