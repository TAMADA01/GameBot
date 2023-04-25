package org.GameBot.GameBot.Bot;

import lombok.SneakyThrows;
import org.GameBot.GameBot.DataBase.DataBase;
import org.GameBot.GameBot.PetFolder.Pet;
import org.GameBot.GameBot.PetFolder.StatusPet;
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
import java.util.function.Consumer;

@Component
public class TelegramBot extends TelegramLongPollingBot implements Runnable {
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

    @Override
    public void run(){
        List<Pet> pets = dataBase.getPets();
        for (var pet : pets) {
            pet.setHunger(pet.getHunger() + 1);
            pet.setCheerfulness(pet.getCheerfulness() - 1);
            dataBase.updatePetCharacteristics(pet);
        }
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
            } else if (callback.startsWith("u_")) {
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
        var userID = update.getCallbackQuery().getData().replace("u_", "");
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
                    writeDescriptionMission(message);
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
            }else if (text.equals("Отмена")) {
                cancelCommand();
            }else if (text.startsWith("/my_pet") || text.equals("Мой питомец")) {
                executeForGroup(message, this::myPetCommand);
            } else if (text.startsWith("/pet_info")) {
                executeForGroup(message, this::petInfoCommand);
            } else if (text.equals("Получить питомца")) {
                executeForGroup(message, this::createPetCommand);
            } else if (text.equals("Выкинуть питомца")) {
                executeForGroup(message, this::deletePetCommand);
            }else if (text.startsWith("Дать питомцу имя ")) {
                executeForGroup(message, this::renamePetCommand);
            } else if (text.equals("Список миссий")) {
                executeForGroup(message, this::getMissionListCommand);
            }else if (text.equals("Уложить питомца спать")) {
                executeForGroup(message, this::sleepCommand);
            }else if (text.equals("Отправить питомца в качалку")) {
                executeForGroup(message, this::goToGymCommand);
            }else if (text.equals("Покормить питомца")) {
                executeForGroup(message, this::feedCommand);
            }else if (text.equals("Мой Инвентарь")) {
                executeForGroup(message, this::showInventoryCommand);
            }else {
                System.out.println("Unexpected command: " + text);
            }

        }
    }

    @SneakyThrows
    private void feedCommand(Message message) {
        executeForUserWithPet(message, this::isFreePetFeed);
    }
    @SneakyThrows
    private void isFreePetFeed(Message message) {
        executeForFreePet(message, this::feedPet);
    }
    @SneakyThrows
    private void feedPet(Message message) {
        dataBase.feedPet(message.getChatId().toString(), message.getFrom().getId().toString(), -10);
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Вы покормили своего питомца")
                .build());
    }

    @SneakyThrows
    private void goToGymCommand(Message message) {
        executeForUserWithPet(message, this::isFreePetGoToGym);
    }
    @SneakyThrows
    private void isFreePetGoToGym(Message message) {
        executeForFreePet(message, this::goToGymPet);
    }
    @SneakyThrows
    private void goToGymPet(Message message) {
        dataBase.goGymPet(message.getChatId().toString(), message.getFrom().getId().toString(), 1);
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Питомец тренируется")
                .build());
    }

    @SneakyThrows
    private void showInventoryCommand(Message message) {
        executeForUserWithPet(message, this::showInventoryPet);
    }
    @SneakyThrows
    private void showInventoryPet(Message message) {
        var result = dataBase.showInventory(message.getChatId().toString(), message.getFrom().getId().toString());
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format("""
                                    Инвентарь питомца:
                                    ❤Аптечки: %d❤
                                    ⚡️Энергетик: %d⚡️
                                    \uD83C\uDF57Уселитель силы: %d\uD83C\uDF57"""
                        , result.getInt("firstAidKit"), result.getInt("energetics"), result.getInt("PowerBooster")))
                .build());
    }

    @SneakyThrows
    private void sleepCommand(Message message) {
        executeForUserWithPet(message, this::isFreeSleepPet);
    }
    @SneakyThrows
    private void isFreeSleepPet(Message message) {
        executeForFreePet(message, this::sleepPet);
    }
    @SneakyThrows
    private  void sleepPet(Message message){
        dataBase.sleepPet(message.getChatId().toString(), message.getFrom().getId().toString(), 10);
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Ваш питомец спит")
                .build());
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
                                .callbackData(String.format("u_%s", result.getInt("userID")))
                                .build()
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
    private void writeDescriptionMission(Message message) {
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
    private void getMissionListCommand(Message message) {
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
        executeForUserWithPet(message, this::petInfo);
    }

    @SneakyThrows
    private void petInfo(Message message) {
        //Добавление Inline клавиатур
        List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                InlineKeyboardButton.builder().text("Уложить спать").switchInlineQueryCurrentChat("Уложить питомца спать").build(),
                InlineKeyboardButton.builder().text("Покормить").switchInlineQueryCurrentChat("Покормить питомца").build()
        );
        List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                InlineKeyboardButton.builder().text("Отправить в качалку").switchInlineQueryCurrentChat("Отправить питомца в качалку").build(),
                InlineKeyboardButton.builder().text("Инвентарь").switchInlineQueryCurrentChat("Мой Инвентарь").build()
        );

        List<List<InlineKeyboardButton>> rowsButton = List.of(
                rowButton1,
                rowButton2
        );

        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format("%s\nДействия с питомцем:\nУложить спать +10 к бодрости\nПокормить -5 к голоду\nОтправить в качалку +1 к силе", getStatusPet(message)))
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                .build());
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
        executeForUserWithPet(message, this::myPet);
    }

    @SneakyThrows
    private void myPet(Message message){
        //Добавление Inline клавиатур
        List<InlineKeyboardButton> rowButton1 = Arrays.asList(
                InlineKeyboardButton.builder().text("Уложить спать").switchInlineQueryCurrentChat("Уложить питомца спать").build(),
                InlineKeyboardButton.builder().text("Покормить").switchInlineQueryCurrentChat("Покормить питомца").build()
        );
        List<InlineKeyboardButton> rowButton2 = Arrays.asList(
                InlineKeyboardButton.builder().text("Отправить в качалку").switchInlineQueryCurrentChat("Отправить питомца в качалку").build(),
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
                                    ❤Здоровье: %s❤
                                    ⚡️Бодрость: %s⚡️
                                    \uD83C\uDF57Голод: %s\uD83C\uDF57
                                    \uD83D\uDCAAСила: %s\uD83D\uDCAA
                                    \uD83D\uDCB0Монет: %s\uD83D\uDCB0
                                    %s"""
                        , pet.name, pet.getHealth(), pet.getCheerfulness(), pet.getHunger(), pet.getPower(), pet.getMoney(), getStatusPet(message)))
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                .build());
    }

    @SneakyThrows
    private void renamePetCommand(Message message) {
        executeForUserWithPet(message, this::renamePet);
    }
    @SneakyThrows
    private void renamePet(Message message) {
        var name = message.getText().replace("Дать питомцу имя ", "");
        dataBase.renamePet(message.getFrom().getId().toString(), message.getChatId().toString(), name);

        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format("Вы переименовали вашего питомца. Теперь его зовут: %s", name))
                .build());

    }

    @SneakyThrows
    private void createPetCommand(Message message) {
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

    @SneakyThrows
    private void deletePetCommand(Message message) {
        executeForUserWithPet(message, this::deletePet);
    }

    @SneakyThrows
    private void deletePet(Message message) {
        dataBase.deletePet(message.getChatId().toString(), message.getFrom().getId().toString());
        dataBase.deleteUser(message.getChatId().toString(), message.getFrom().getId().toString());
        executeAsync(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Вы выкинули питомца\uD83D\uDE2D")
                .build());
    }

    private String getStatusPet(Message message){
        Pet pet = dataBase.getPet(message.getChatId().toString(), message.getFrom().getId().toString());
        var time = Pet.WORKING_TIME - (System.currentTimeMillis()/1000L - pet.time)/60;
        var hours = time / 60 % 24;
        var minutes = time % 60;
        if (time <= 0){
            pet.status = StatusPet.None.name();
            dataBase.updateStatus(message.getChatId().toString(), message.getFrom().getId().toString(), StatusPet.None);
        }
        String text;
        if (pet.status.equals(StatusPet.Eat.name())){
            text = String.format("Ваш питомец ест. Он будет занят в течении %d часов %d минут", hours, minutes);
        }else if (pet.status.equals(StatusPet.Sleep.name())){
            text = String.format("Ваш питомец спит. Он будет занят в течении %d часов %d минут", hours, minutes);
        }else if (pet.status.equals(StatusPet.ToGym.name())){
            text = String.format("Ваш питомец тренируется. Он будет занят в течении %d часов %d минут", hours, minutes);
        }else {
            text = "Ваш питомец свободен";
        }
        return text;
    }

    @SneakyThrows
    public void executeForGroup(Message message, Consumer<Message> command){
        if (!message.getChat().isUserChat()) {
            command.accept(message);
        } else {
            executeAsync(SendMessage.builder()
                    .chatId(message.getFrom().getId())
                    .text("Данная команда возможна только в групповом чате")
                    .build());
        }
    }

    @SneakyThrows
    private  void executeForUserWithPet(Message message, Consumer<Message> command){
        boolean havePet = dataBase.havePet(message.getChatId().toString(), message.getFrom().getId().toString());
        if (havePet){
            command.accept(message);
        }
        else {
            List<InlineKeyboardButton> rowButton = Collections.singletonList(
                    InlineKeyboardButton.builder().text("Получит питомца").switchInlineQueryCurrentChat("Получить питомца").build()
            );
            List<List<InlineKeyboardButton>> rowsButton = List.of(
                    rowButton
            );
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У вас нет питомца, но вы можете его получит.")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsButton).build())
                    .build());
        }
    }

    @SneakyThrows
    private  void executeForFreePet(Message message, Consumer<Message> command){
        Pet pet = dataBase.getPet(message.getChatId().toString(), message.getFrom().getId().toString());
        var time = Pet.WORKING_TIME - (System.currentTimeMillis()/1000L - pet.time)/60;
        if (time <= 0){
            pet.status = StatusPet.None.name();
            dataBase.updateStatus(message.getChatId().toString(), message.getFrom().getId().toString(), StatusPet.None);
        }
        if (pet.status.equals(StatusPet.None.name())){
            command.accept(message);
        }
        else {
            executeAsync(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Ваш питомец занят. Попробуйте в другой раз")
                    .build());
        }
    }

    public void everyHours(){

    }
}
