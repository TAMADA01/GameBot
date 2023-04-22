package org.GameBot.GameBot.Bot;

import lombok.SneakyThrows;
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

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    BotConfig config;
    DataBase dataBase;
    StateBot state;
    private Map<String, String> storage;

    public TelegramBot(BotConfig config, DataBase dataBases){
        this.config = config;
        this.dataBase = dataBases;
        state = StateBot.None;
        storage = new HashMap<String, String>();

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

    @SneakyThrows
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
        executeAsync(smc);
    }
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        //Проверка на текстовое сообщение
        if (update.hasMessage()){
            handlerMessage(update.getMessage());
        }
        //Проверка на сообщение от InlineButton
        else if (update.hasCallbackQuery()){
            handlerCallback(update);
        }
    }

    private void handlerCallback(Update update) {
        var callback = update.getCallbackQuery().getData();
        if (callback.startsWith("mission_id")) {
            getReward(update);
        }else {
            System.out.println("Unexpected callback: " + callback);
        }
    }

    @SneakyThrows
    private void getReward(Update update) {
        var id = update.getCallbackQuery().getData().replace("mission_id", "");
        var userID = update.getCallbackQuery().getFrom().getId().toString();
        var chatID = update.getCallbackQuery().getMessage().getChatId().toString();
        var reward = dataBase.getReward(id);
        int money = dataBase.getMoney(userID, chatID);
        money += reward;
        dataBase.setReward(userID, chatID ,id, String.valueOf(money));

        executeAsync(SendMessage.builder()
                .chatId(chatID)
                .text("Награда начислена")
                .build());
    }

    @SneakyThrows
    private void handlerMessage(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (message.hasEntities()){
                var entities = message.getEntities();
                for (var entity : entities) {
                    if (entity.getText().equals("@AXELTeam_bot")){
                        text = text.replace("@AXELTeam_bot ", "");
                        break;
                    }
                }
            }
            //Список команд
            if ((text.startsWith("/start") || text.startsWith("/help"))) {
                helpCommand(message);
            } else if (text.startsWith("/my_pet")) {
                myPetCommand(message);
            } else if (text.startsWith("/pet_info")) {
                petInfoCommand(message);
            } else if (text.equals("Получить питомца")) {
                createPet(message);
            } else if (text.startsWith("Дать питомцу имя ")) {
                renamePet(message);
            } else if (text.equals("Список миссий")) {
                getMissionList(message);
            } else if (text.equals("Создать миссию")) {
                createMission(message);
            } else if (text.startsWith("Наградить ")) {
                missionSelection(message);
            }else if (state == StateBot.WriteDiscription) {
                writeDiscriptionMission(message);
            }else if (state == StateBot.WriteReward) {
                writeRewardMission(message);
            }else {
                System.out.println("Unexpected command: " + text);
            }
        }
    }

    @SneakyThrows
    private void missionSelection(Message message) {
        if (message.hasEntities()) {
            System.out.println(message.getEntities().get(0).toString());
            //var user = message.getEntities().get(1).getUser();
            //boolean havePet = dataBase.havePet(message.getChatId().toString(), user.getId().toString());
            if (true) {
                var result = dataBase.getMissionList(message.getChatId().toString());
                if (result.isBeforeFirst()) {
                    //Добавление Inline клавиатур
                    List<List<InlineKeyboardButton>> rowsButton = new ArrayList<>();
                    int number = 1;
                    while (result.next()) {
                        var text = String.format("\uD83D\uDFE2 %s) %s Наград: %s монет\n", number, result.getString("description"), result.getInt("reward"));
                        List<InlineKeyboardButton> rowButton = Arrays.asList(
                                InlineKeyboardButton.builder()
                                        .text(text)
                                        .callbackData(String.format("mission_id%s", result.getInt("id"))).build()
                        );
                        rowsButton.add(rowButton);
                        number++;
                    }

                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("Список миссий:")
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                            .build());
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text("Сейчас нет действующих миссий")
                            .build());
                }
            }
            else{
                /*executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text(String.format("У %s нет питомца", user.getFirstName()))
                        .build());*/
            }
        }else{
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Вы не указали кого наградить")
                    .build());
        }
    }

    @SneakyThrows
    private void writeRewardMission(Message message) {
        state = StateBot.None;
        dataBase.createMission(message.getChatId().toString(), storage.get("description"), message.getText());
        storage.clear();
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Поздравляю, миссия создана")
                .build());
    }

    @SneakyThrows
    private void writeDiscriptionMission(Message message) {
        state = state.nextState();
        storage.put("description", message.getText());
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Напишите количество монет в качестве награды за выполнение миссии")
                .build());
    }

    @SneakyThrows
    private void createMission(Message message) {
        state = StateBot.WriteDiscription;
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Для создания миссии напишите описание")
                .build());
    }

    @SneakyThrows
    private void getMissionList(Message message) {
        var result = dataBase.getMissionList(message.getChatId().toString());
        if (result.isBeforeFirst()) {
            StringBuilder text = new StringBuilder("Список миссий:\n");
            int number = 1;
            while (result.next()) {
                text.append(String.format("\uD83D\uDFE2 %s) %s Наград: %s монет\n", number, result.getString("description"), result.getInt("reward")));
                number++;
            }

            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(text.toString())
                    .build());
        }
        else{
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Сейчас нет действующих миссий")
                    .build());
        }
    }

    //Показать информацию о питомце
    @SneakyThrows
    private void petInfoCommand(Message message) {
        if (message.getChat().isGroupChat()) {
            boolean havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
            if (havePet){
                //Добавление Inline клавиатур
                List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Уложить спать").switchInlineQueryCurrentChat("Уложить спать").build(),
                        InlineKeyboardButton.builder().text("Покормить").switchInlineQueryCurrentChat("Покормить").build()
                );
                List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Отправить в качалку").switchInlineQueryCurrentChat("Отправить в качалку").build(),
                        InlineKeyboardButton.builder().text("Инвентарь").switchInlineQueryCurrentChat("Мой Инвентарь").build()
                );

                List<List<InlineKeyboardButton>> rowsButton = List.of(
                        rowButton1,
                        rowButton2
                );

                executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Действия с питомцем:\nУложить спать +10 к бодрости\nПокормить -5 к голоду\nОтправить в качалку +1 к силе")
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                        .build());
            }
            else {
                executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("У вас нет питомца, но вы можете его получит.\nНапишите команду: `Получить питомца`")
                        .parseMode("Markdown")
                        .build());
            }
        }
        else {
            executeAsync(SendMessage.builder()
                    .chatId(message.getFrom().getId())
                    .text("Данная команда возможна только в групповом чате")
                    .build());
        }
    }

    //Команда /help
    @SneakyThrows
    private void helpCommand(Message message){
        executeAsync(SendMessage.builder()
                .chatId(message.getChat().getId())
                .text("Список команд и всё такое")
                .build());
    }

    //Показать питомца
    @SneakyThrows
    private void myPetCommand(Message message) {
        if (message.getChat().isGroupChat()) {
            boolean havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
            if (havePet){
                //Добавление Inline клавиатур
                List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Уложить спать").switchInlineQueryCurrentChat("Уложить спать").build(),
                        InlineKeyboardButton.builder().text("Покормить").switchInlineQueryCurrentChat("Покормить").build()
                );
                List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                        InlineKeyboardButton.builder().text("Отправить в качалку").switchInlineQueryCurrentChat("Отправить в качалку").build(),
                        InlineKeyboardButton.builder().text("Инвентарь").switchInlineQueryCurrentChat("Мой Инвентарь").build()
                );

                List<List<InlineKeyboardButton>> rowsButton = List.of(
                        rowButton1,
                        rowButton2
                );

                Pet pet = dataBase.getPet(message.getChatId().toString(), message.getFrom().getId().toString());
                executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text(String.format("Имя питомца: %s\n" +
                                "Здоровье: %s❤\n" +
                                "Голод: %s\uD83C\uDF57\n" +
                                "Сила: %s\uD83D\uDCAA\n" +
                                "Монет: %s\uD83D\uDCB0"
                                , pet.name, pet.health, pet.hunger, pet.power, pet.money))
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                        .build());
            }
            else {
                executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("У вас нет питомца, но вы можете его получит.\nНапишите команду: `Получить питомца`")
                        .parseMode("Markdown")
                        .build());
            }
        }
        else {
            executeAsync(SendMessage.builder()
                    .chatId(message.getFrom().getId())
                    .text("Данная команда возможна только в групповом чате")
                    .build());
        }
    }

    @SneakyThrows
    private void renamePet(Message message) {
        boolean havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
        if (havePet) {
            var name = message.getText().replace("Дать питомцу имя ", "");
            dataBase.renamePet(message.getFrom().getId().toString(), message.getChatId().toString(), name);

            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(String.format("Вы переименовали вашего питомца. Теперь его зовут: %s", name))
                    .build());
        }
        else{
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас нет питомца, но вы можете его получит.\nНапишите команду: `Получить питомца`")
                    .parseMode("Markdown")
                    .build());
        }
    }

    @SneakyThrows
    private void createPet(Message message) {
        boolean havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
        if (havePet){
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас уже есть питомец")
                    .build());
        }
        else {
            dataBase.createPet(message.getFrom().getId().toString(), message.getChatId().toString(), "Ваш питомец");

            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас появился питомец\uD83D\uDE0D")
                    .build());
            myPetCommand(message);
        }
    }
}
