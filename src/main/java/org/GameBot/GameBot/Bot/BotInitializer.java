package org.GameBot.GameBot.Bot;

import org.GameBot.GameBot.DataBase.DataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.SQLException;

@Component
public class BotInitializer {
    @Autowired
    TelegramBot bot;
    @Autowired
    DataBase dataBase;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException, SQLException {
        /*var sqlQuery = dataBase.select("*", "pets", "");
        dataBase.query(sqlQuery);
        System.out.println(dataBase.result.getInt("health"));*/
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);

    }
}
