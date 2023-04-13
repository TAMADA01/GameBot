package telegrambot;

import telegrambot.Bot.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        //Регистрация бота
        final String botToken = "6048540644:AAENevqDQr90auJC2lTslo94oWE6JyjWP4E";
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TelegramBot(botToken));

        System.out.println("**** Bot start ****");
    }
}