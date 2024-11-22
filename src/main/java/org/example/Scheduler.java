package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static final ChatIdRecorderBot BOT = new ChatIdRecorderBot();

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                System.out.println("Запускается процесс добавления пользователей и парсинг...");
                BOT.checkForNewUsers();
                Set<String> userIds = loadUserIdsFromFile();
                for (String userId : userIds) {
                    CianParser.parseAndNotify(userId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
    }

    private static Set<String> loadUserIdsFromFile() {
        Set<String> userIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                userIds.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userIds;
    }
}
