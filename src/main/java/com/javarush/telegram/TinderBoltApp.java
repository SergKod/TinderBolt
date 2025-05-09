package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "javarush_second_project_ai_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "8070057101:AAGaPp_gZtP-rNWiR8sIWPXKgD4B-pHPdh4"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:AUFMFhaIxsLXr0B-4fw5Ik4FWbxdKoFXzX3lK0-eGXhH5LQrJfGAQzs5F44VgDQ4LEuCL6c6A4JFkblB3TJujlqox1oLzy0gB77UvCxpwpxiYZ5mnUTAaIold1B09jsDr0EPRetAXxuWnkyDagxA9OMCJZ__"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo she;
    private int questionCount;


    public TinderBoltApp() {

        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if(message.equals("/start")){  // проверка веденого текста с "/start", если ок,то выполняются следующие методы:
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main"); // Вводим в качестве параметра фото с images
            String text = loadMessage("main"); // загружаем текст, который будет после фото
            sendTextMessage(text); // выводим на экран данный згруженный текст, который будет видеть пользователь

            showMainMenu("главное меню бота","/start", // вводим в отдельное контекстное меню,для удобства пользования
                    "Общение с ChatGPT \uD83D\uDE0E","/profile",
                    "сообщение для знакомства \uD83E\uDD70","/opener",
                    "переписка от вашего имени \uD83D\uDE08","/message",
                    "переписка со звездами \uD83D\uDD25","/date",
                    "задать вопрос чату GPT \uD83E\uDDE0","/gpt");
            return;
        }

// command GPT
        if(message.equals("/gpt")){ // проверка веденого текста с "/gpt", если ок,то выполняются следующие методы:
            currentMode = DialogMode.GPT;
            sendPhotoMessage("avatar_main"); // Вводим в качестве параметра фото с images
            String gpt = loadMessage("gpt"); // загружаем текст, который будет после фото
            sendTextMessage(gpt); // выводим на экран данный згруженный текст, который будет видеть пользователь
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()){
            String promt = loadPrompt("gpt"); // загружаем промт - команду чату GPT,какое дейтсвие будет выполнять
            Message msg = sendTextMessage("Подождите пару секунд ChatGPT думает ....");
            String answer = chatGPT.sendMessage(promt, message); // коммуникация чата GPT с сообщением пользователя и дальнейшим поиском ответа
            updateTextMessage(msg,answer);// вывод овтета чата GPT
            return;
        }

// command DATE
        if(message.equals("/date")){
            currentMode = DialogMode.DATE;// Переводим диалог в режим date
            sendPhotoMessage("date");// Приветственное фото в команде
            String text = loadMessage("date");// загружаем текст, который будет после фото
            sendTextButtonsMessage(text,
                    "Ариана Гранде","date_grande",
                    "Марго Робби","date_robbie",
                    "Зендея","date_zendaya",
                    "Райн Гослинг","date_gosling",
                    "Том Харди","date_hardy");
            return;
        }
        if (currentMode==DialogMode.DATE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("date_")){
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор! \nТвоя задача пригласить девушку/парня на свидание ❤\uFE0F за 5 сообщений");

                String promt =loadPrompt(query);
                chatGPT.setPrompt(promt);
                return;

            }
            Message msg = sendTextMessage("девушка печатает ....");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg,answer);
            return;
        }

// command MESSAGE
        if(message.equals("/message")){
            currentMode = DialogMode.MESSAGE;// Переводим диалог в режим message
            sendPhotoMessage("message");// Приветственное фото в команде
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение","message_next",
                    "Пригласить на свидание","message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String promt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите пару секунд ChatGPT думает ....");
                String answer = chatGPT.sendMessage(promt, userChatHistory); // 10 сек
                updateTextMessage(msg, answer);
                return;
            }
            list.add(message);
            return;
        }

// command PROFILE
        if(message.equals("/profile")){
            currentMode = DialogMode.PROFILE;// Переводим диалог в режим profile
            sendPhotoMessage("profile");// Приветственное фото в команде

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Cколько вам лет?");
            return;
        }
        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("Кем вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("У вас есть хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Что вам не нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цели знакомства?");
                    return;
                case 5:
                    me.goals = message;


                    String aboutMyself = me.toString();
                    String promt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите пару секунд ChatGPT думает\uD83E\uDDE0 ....");
                    String answer = chatGPT.sendMessage(promt, aboutMyself);
                    updateTextMessage(msg,answer);
                    return;
            }
            return;
        }
// command OPENER
        if(message.equals("/opener")){
            currentMode=DialogMode.OPENER;// Переводим диалог в режим opener
            sendPhotoMessage("opener");// Приветственное фото в команде

            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки?");
            return;
        }

        if(currentMode==DialogMode.OPENER && !isMessageCommand()){

            switch (questionCount){
                case 1:
                    she.name=message;
                    questionCount=2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age=message;
                    questionCount=3;
                    sendTextMessage("Есть ли у нее хобби?");
                    return;
                case 3:
                    she.hobby=message;
                    questionCount=4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation=message;
                    questionCount=5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals=message;

                    String aboutFriend = message;
                    String aboutMyself = me.toString();
                    String promt = loadPrompt("opener");

                    Message msg = sendTextMessage("Подождите пару секунд ChatGPT думает\uD83E\uDDE0 ....");
                    String answer = chatGPT.sendMessage(promt, aboutFriend);
                    updateTextMessage(msg,answer);
                    return;
            }

            return;
        }


        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextMessage("Вы написали:" + message);
        sendTextButtonsMessage("Выберите режим работы:",
                "Старт","start",
                "Стоп","stop");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
