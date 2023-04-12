package telegrambot.Bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
                new BotCommand("/help", "Помощь"),
                new BotCommand("/pet_info", "Информация о питомце"),
                new BotCommand("/my_pet", "Мой питомец")
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
        if (update.hasMessage()){
            handlerMessage(update.getMessage());
        }
        //Проверка на сообщение от InlineButton
        else if (update.hasCallbackQuery()){
            try {
                execute(SendMessage.builder()
                        .chatId(update.getCallbackQuery().getMessage().getChatId())
                        .text("Callback = " + update.getCallbackQuery().getData())
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handlerMessage(Message message) {
        if (message.hasText()) {

            //Список команд
            switch (message.getText()) {
                case "/start", "/help" -> helpCommand(message);
                case "/my_pet" -> myPetCommand(message);
                case "/pet_info" -> petInfoCommand(message);
                default -> System.out.println("Unexpected command: " + message.getText());
            }
        }
    }

    //Показать информацию о питомце
    private void petInfoCommand(Message message) {
        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("PetInfo")
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    //Команда /start
    private void startCommand(Message message){
        try {
            execute(SendMessage.builder()
                    .chatId(message.getChat().getId())
                    .text("Start work. Good!\n" + message.getChat().getId() + "\n" + message.getFrom().getId())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Команда /help
    private void helpCommand(Message message){
        try {
            execute(SendMessage.builder()
                    .chatId(message.getChat().getId())
                    .text("Список команд и всё такое")
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Показать питомца
    private void myPetCommand(Message message){
        if (message.getChat().isGroupChat()) {
            //Добавление Inline клавиатур
            List<InlineKeyboardButton> rowButton = Arrays.asList(
                    InlineKeyboardButton.builder().text("Test1").callbackData("test1_data").build(),
                    InlineKeyboardButton.builder().text("Test2").callbackData("test2_data").build()
            );

            List<List<InlineKeyboardButton>> rowsButton = List.of(
                    rowButton
            );

            try {
                execute(SendMessage.builder()
                        .chatId(message.getChat().getId())
                        .text("Show stats!!!")
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                execute(SendMessage.builder()
                        .chatId(message.getFrom().getId())
                        .text("Данная команда возможна только в групповом чате")
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
