package org.example.Bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot{

    private String BotName = "AXEL";
    private String BotToken = "6048540644:AAENevqDQr90auJC2lTslo94oWE6JyjWP4E";

    public TelegramBot(){
        SetMyCommands myCommands = new SetMyCommands();
    }

    @Override
    public String getBotUsername() {
        return BotName;
    }

    @Override
    public String getBotToken(){
        return BotToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){

            String chatID = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            switch (messageText) {
                case "/start" -> startCommand(chatID);
                default -> System.out.println("Unexpected message");
            }
        }
    }

    private void startCommand(String chatID){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText("Start work. Good!");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
