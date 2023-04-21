package org.GameBot.GameBot.Bot;

import org.GameBot.GameBot.DataBase.DataBase;
import org.GameBot.GameBot.PetFolder.Pet;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    BotConfig config;
    DataBase dataBase;
    StateBot state;

    public TelegramBot(BotConfig config, DataBase dataBases){
        this.config = config;
        this.dataBase = dataBases;
        state = StateBot.None;

        setMyCommands();
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
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
            executeAsync(smc);
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
                executeAsync(SendMessage.builder()
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
            String text = message.getText();
            if ((text.equals("/start") || text.equals("/help")) && state == StateBot.None) {
                helpCommand(message);
            } else if (text.equals("/my_pet") && state == StateBot.None) {
                myPetCommand(message);
            } else if (text.equals("/pet_info") && state == StateBot.None) {
                petInfoCommand(message);
            } else if (text.equals("Получить питомца") && state == StateBot.None) {
                createPet(message);
            } else if (text.startsWith("Дать питомцу имя ")  && state == StateBot.None) {
                renamePet(message);
            } else {
                System.out.println("Unexpected command: " + message.getText());
            }
        }
    }

    //Показать информацию о питомце
    private void petInfoCommand(Message message) {
        if (message.getChat().isGroupChat()) {
            boolean havePet;
            try {
                havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
            } catch (SQLException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
            if (havePet){
                //Добавление Inline клавиатур
                List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Уложить спать").callbackData("sleep").build(),
                        InlineKeyboardButton.builder().text("Покормить").callbackData("feed").build()
                );
                List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Отправить в качалку").callbackData("go_gym").build(),
                        InlineKeyboardButton.builder().text("Инвентарь").callbackData("open_inventory").build()
                );

                List<List<InlineKeyboardButton>> rowsButton = List.of(
                        rowButton1,
                        rowButton2
                );
                try {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("Действия с питомцем:\nУложить спать +10 к бодрости\nПокормить -5 к голоду\nОтправить в качалку +1 к силе")
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("У вас нет питомца")
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            try {
                executeAsync(SendMessage.builder()
                        .chatId(message.getFrom().getId())
                        .text("Данная команда возможна только в групповом чате")
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Команда /help
    private void helpCommand(Message message){
        try {
            executeAsync(SendMessage.builder()
                    .chatId(message.getChat().getId())
                    .text("Список команд и всё такое")
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Показать питомца
    private void myPetCommand(Message message) {
        if (message.getChat().isGroupChat()) {
            boolean havePet;
            try {
                havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
            } catch (SQLException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
            if (havePet){
                //Добавление Inline клавиатур
                List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Уложить спать").callbackData("sleep").build(),
                        InlineKeyboardButton.builder().text("Покормить").callbackData("feed").build()
                );
                List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Отправить в качалку").callbackData("go_gym").build(),
                        InlineKeyboardButton.builder().text("Инвентарь").callbackData("open_inventory").build()
                );

                List<List<InlineKeyboardButton>> rowsButton = List.of(
                        rowButton1,
                        rowButton2
                );

                Pet pet;
                try {
                    pet = dataBase.getPet(message.getChatId().toString(), message.getFrom().getId().toString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(String.format("Имя питомца: %s\nЗдоровье: %s❤\nГолод: %s\uD83C\uDF57", pet.name, pet.health, pet.hunger))
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("У вас нет питомца, но вы можете его получит.\nНапишите команду: `Получить питомца`")
                            .parseMode("Markdown")
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            try {
                executeAsync(SendMessage.builder()
                        .chatId(message.getFrom().getId())
                        .text("Данная команда возможна только в групповом чате")
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void renamePet(Message message) {
        var name = message.getText().split("Дать питомцу имя ")[1];
        try {
            dataBase.renamePet(message.getFrom().getId().toString(), message.getChatId().toString(), name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(String.format("вы переименовали вашего питомца. Теперь его зовут: %s", name))
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPet(Message message) {
        try {
            dataBase.createPet(message.getFrom().getId().toString(), message.getChatId().toString(), "Ваш питомец");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас появился питомец\uD83D\uDE0D")
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        myPetCommand(message);
    }
}
