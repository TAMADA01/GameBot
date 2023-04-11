package telegrambot.Bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {

    final String botName = "AXEL";
    @lombok.Getter
    final String botToken = "6048540644:AAENevqDQr90auJC2lTslo94oWE6JyjWP4E";

    public TelegramBot(){
        super();

        setMyCommands();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    private void setMyCommands(){
        //Назначить команды боту. Имя и описание
        List<BotCommand> botCommands = Arrays.asList(
                new BotCommand("/start", "Start work!"),
                new BotCommand("/help", "Help menu"),
                new BotCommand("/show", "Show info")
        );

        SetMyCommands smc = new SetMyCommands();
        smc.setCommands(botCommands);
        //Выполнить загрузку команд
        try {
            this.execute(smc);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onUpdateReceived(Update update) {

        //Проверка на текстовое сообщение
        if (update.hasMessage() && update.getMessage().hasText()){

            Chat chat = update.getMessage().getChat();
            String messageText = update.getMessage().getText();

            //Список команд
            switch (messageText) {
                case "/start" -> startCommand(chat);
                case "/help" -> helpCommand(chat);
                case "/show" -> showCommand(chat);
                default -> System.out.println("Unexpected command: " + messageText);
            }
        }
        //Проверка на сообщение от InlineButton
        else if (update.hasCallbackQuery()){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            sendMessage.setText("Callback = " + update.getCallbackQuery().getData());

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Команда /start
    private void startCommand(Chat chat){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("Start work. Good!");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Команда /help
    private void helpCommand(Chat chat){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("I help you! But later)");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Команда /show
    private void showCommand(Chat chat){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("Show stats!!!");

        //Добавление Inline клавиатуры
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsButton = new ArrayList<>();
        List<InlineKeyboardButton> rowButton = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("Test1");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Test2");

        button1.setCallbackData("test1_data");
        button2.setCallbackData("test2_data");

        rowButton.add(button1);
        rowButton.add(button2);

        rowsButton.add(rowButton);

        markupInline.setKeyboard(rowsButton);
        sendMessage.setReplyMarkup(markupInline);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
