package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ChatIdRecorderBot extends TelegramLongPollingBot {

    private static final String USERS_FILE = "users.txt";

    @Override
    public String getBotUsername() {
        return "CianBot";
    }

    @Override
    public String getBotToken() {
        return "7666432236:AAGqKRqw7hDpUTm0QEDFbHZsvhK1Jnb7yOo";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            String chatId = update.getMessage().getChatId().toString();
            System.out.println("Получено сообщение от пользователя с chat_id: " + chatId);
            addUserIdToFile(chatId);
            sendNotification(chatId, "Привет! Я бот для рассылки объявлений с Cian");
        }
    }

    public void sendNotification(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addUserIdToFile(String chatId) {
        Set<String> userIds = loadUserIdsFromFile();
        if (!userIds.contains(chatId)) {
            userIds.add(chatId);
            saveUserIdsToFile(userIds);
        }
    }

    private Set<String> loadUserIdsFromFile() {
        Set<String> userIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                userIds.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    private void saveUserIdsToFile(Set<String> userIds) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (String userId : userIds) {
                writer.write(userId);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkForNewUsers() {
        System.out.println("Инициализация проверки новых пользователей...");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
