package org.GameBot.GameBot.Bot;

import lombok.SneakyThrows;
import org.GameBot.GameBot.DataBase.DataBase;
import org.GameBot.GameBot.PetFolder.Pet;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    BotConfig config;
    DataBase dataBase;
    StateBot state;
    private final Map<String, String> storage;

    public TelegramBot(BotConfig config, DataBase dataBases){
        this.config = config;
        this.dataBase = dataBases;
        state = StateBot.None;
        storage = new HashMap<>();
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
        //execute(GetChatAdministrators.builder().chatId().build());
    }
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        //System.out.println(update.toString());
        //Проверка на текстовое сообщение
        if (update.hasMessage()){
            if (update.getMessage().getLeftChatMember() != null){
                if(update.getMessage().getLeftChatMember().getUserName().equals("AXELTeam_bot")){
                    dataBase.deleteChat(update.getMessage().getChatId().toString());
                    System.out.println("Бот удален из чата " + update.getMessage().getChatId());
                }else {
                    System.out.println(update.getMessage().getLeftChatMember().getFirstName() + " удален из чата " + update.getMessage().getChatId());
                }
            } else if (!update.getMessage().getNewChatMembers().isEmpty()) {
                if(update.getMessage().getNewChatMembers().get(0).getUserName().equals("AXELTeam_bot")){
                    dataBase.addChat(update.getMessage().getChatId().toString());
                    System.out.println("Бот добавлен в чат " + update.getMessage().getChatId());
                }else {
                    System.out.println(update.getMessage().getLeftChatMember().getFirstName() + "  добавлен в чат " + update.getMessage().getChatId());
                }
            }else{
                handlerMessage(update.getMessage());
            }
        }
        //Проверка на сообщение от InlineButton
        else if (update.hasCallbackQuery()){
            handlerCallback(update);
        } else if (update.hasMyChatMember()) {
            System.out.println(update.getMyChatMember().getNewChatMember().toString());
        }
    }

    private void handlerCallback(Update update) {
        var callback = update.getCallbackQuery().getData();
        if (dataBase.isAdministrator(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getFrom().getId().toString())) {
            if (callback.startsWith("m_id") && state == StateBot.RewardUser) {
                setMissionID(update);
                state = StateBot.None;
            } else if (callback.startsWith("m_id") && state == StateBot.FinishMission) {
                finishMissionCallback(update);
                state = StateBot.None;
            } else if (callback.startsWith("u_id")) {
                getReward(update);
            } else {
                System.out.println("Unexpected callback: " + callback);
            }
        }
    }

    @SneakyThrows
    private void finishMissionCallback(Update update) {
        var id = update.getCallbackQuery().getData().replace("m_id", "");
        dataBase.deleteMission(id);
        executeAsync(SendMessage.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .text("Миссия завершена")
                .build());
    }

    @SneakyThrows
    private void setMissionID(Update update) {
        var id = update.getCallbackQuery().getData().replace("m_id", "");
        storage.put("m_id", id);
        userSelection(update.getCallbackQuery().getMessage());
    }


    @SneakyThrows
    private void getReward(Update update) {
        var userID = update.getCallbackQuery().getData().replace("u_id", "");
        var chatID = update.getCallbackQuery().getMessage().getChatId().toString();
        var reward = dataBase.getReward(storage.get("m_id"));
        storage.clear();
        int money = dataBase.getMoney(userID, chatID);
        money += reward;
        dataBase.setReward(userID, chatID, String.valueOf(money));

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

            if (dataBase.isAdministrator(message.getChatId().toString(), message.getFrom().getId().toString())){
                if (text.equals("Создать миссию")) {
                    createMission(message);
                    return;
                } else if (text.equals("Завершить миссию")) {
                    closeMission(message);
                    return;
                } else if (text.equals("Наградить участника")) {
                    rewardUser(message);
                    return;
                } else if (state == StateBot.WriteDiscription) {
                    writeDiscriptionMission(message);
                    return;
                } else if (state == StateBot.WriteReward) {
                    writeRewardMission(message);
                    return;
                }else if (text.equals("Обновить администраторов")) {
                    updateAdministrators(message);
                    return;
                }
            }

            //Список команд
            if ((text.startsWith("/start") || text.startsWith("/help"))) {
                helpCommand(message);
            }else if (text.startsWith("/my_pet")) {
                if (!message.getChat().isUserChat()) {
                    myPetCommand(message);
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getFrom().getId())
                            .text("Данная команда возможна только в групповом чате")
                            .build());
                }
            } else if (text.startsWith("/pet_info")) {
                if (!message.getChat().isUserChat()) {
                    petInfoCommand(message);
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getFrom().getId())
                            .text("Данная команда возможна только в групповом чате")
                            .build());
                }
            } else if (text.equals("Получить питомца")) {
                if (!message.getChat().isUserChat()) {
                    createPet(message);
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getFrom().getId())
                            .text("Данная команда возможна только в групповом чате")
                            .build());
                }
            } else if (text.startsWith("Дать питомцу имя ")) {
                if (!message.getChat().isUserChat()) {
                    renamePet(message);
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getFrom().getId())
                            .text("Данная команда возможна только в групповом чате")
                            .build());
                }
            } else if (text.equals("Список миссий")) {
                if (!message.getChat().isUserChat()) {
                    getMissionList(message);
                } else {
                    executeAsync(SendMessage.builder()
                            .chatId(message.getFrom().getId())
                            .text("Данная команда возможна только в групповом чате")
                            .build());
                }
            }else if (text.equals("Отмена")) {
                cancelCommand();
            }else {
                System.out.println("Unexpected command: " + text);
            }

        }
    }

    private void cancelCommand() {
        state = StateBot.None;
        storage.clear();
    }

    @SneakyThrows
    private void updateAdministrators(Message message) {
        dataBase.deleteAdministrators(message.getChatId().toString());
        GetChatAdministrators getChatAdministrators = GetChatAdministrators.builder().chatId(message.getChatId()).build();
        var administrators = execute(getChatAdministrators);
        for (var admin : administrators) {
            dataBase.addAdministrators(message.getChatId().toString(), admin.getUser().getId().toString());
        }
    }

    private void rewardUser(Message message) {
        state = StateBot.RewardUser;
        missionSelection(message);
    }

    @SneakyThrows
    private void userSelection(Message message) {
        var result = dataBase.getUserList(message.getChatId().toString());
        if (result.isBeforeFirst()) {
            //Добавление Inline клавиатур
            List<List<InlineKeyboardButton>> rowsButton = new ArrayList<>();
            while (result.next()) {
                var text = String.format("%s %s\n", result.getString("firstName"), result.getString("lastName"));
                List<InlineKeyboardButton> rowButton = Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(text)
                                .callbackData(String.format("u_id%s", result.getInt("userID"))).build()
                );
                rowsButton.add(rowButton);
            }

            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Список участников с питомцами:")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                    .build());
        } else {
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Ни у кого нет питомца в чате")
                    .build());
        }
    }
    @SneakyThrows
    private void missionSelection(Message message) {
        var result = dataBase.getMissionList(message.getChatId().toString());
        if (result.isBeforeFirst()) {
            //Добавление Inline клавиатур
            List<List<InlineKeyboardButton>> rowsButton = new ArrayList<>();
            int number = 1;
            while (result.next()) {
                var text = String.format("\uD83D\uDFE2 %s) %s Наград: %s монет\n", number, result.getString("description"), result.getInt("reward"));
                List<InlineKeyboardButton> rowButton = Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(text)
                                .callbackData(String.format("m_id%s", result.getInt("id"))).build()
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
    private void closeMission(Message message) {
        state = StateBot.FinishMission;
        missionSelection(message);
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
                    .text(String.format("""
                                    Имя питомца: %s
                                    Здоровье: %s❤
                                    Голод: %s\uD83C\uDF57
                                    Сила: %s\uD83D\uDCAA
                                    Монет: %s\uD83D\uDCB0"""
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
            dataBase.addUser(message.getChatId().toString(), message.getFrom());
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас появился питомец\uD83D\uDE0D")
                    .build());
            myPetCommand(message);
        }
    }
}
