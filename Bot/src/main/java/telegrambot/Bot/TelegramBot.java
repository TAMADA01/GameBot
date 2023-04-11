package telegrambot.Bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {

    private String botName = "AXEL";
    private String botToken = "6048540644:AAENevqDQr90auJC2lTslo94oWE6JyjWP4E";

    public TelegramBot(){
        super();
        setMyCommands();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void setMyCommands(){
        List<BotCommand> botCommands = Arrays.asList(
                new BotCommand("/start", "Start work!"),
                new BotCommand("/help", "Help menu")
        );

        SetMyCommands smc = new SetMyCommands();
        smc.setCommands(botCommands);

        try {
            this.execute(smc);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){

            String chatID = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            switch (messageText) {
                case "/start" -> startCommand(chatID);
                case "/help" -> helpCommand(chatID);
                default -> System.out.println("Unexpected command: " + messageText);
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
            throw new RuntimeException(e);
        }
    }

    private  void helpCommand(String chatID){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText("I help you! But later)");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
