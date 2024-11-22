package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CianParser {

    private static final String LINKS_FILE = "cian_links.txt";
    private static final String PROCESSED_USERS_FILE = "processed_users.txt";
    private static final TelegramBot BOT = new TelegramBot();

    public static void parseAndNotify(String userId) {
        try {
            String url = "https://www.cian.ru/cat.php?currency=2&deal_type=rent&engine_version=2&location%5B0%5D=4760&maxprice=45000&offer_type=flat&room1=1&room2=1&type=4";

            Document doc = Jsoup.connect(url).get();

            Elements links = doc.select("a[href^='https://www.cian.ru/rent/flat/']");
            List<String> list = new ArrayList<>();
            for (Element link : links) {
                list.add(link.attr("href"));
            }

            String regex = "^https://www\\.cian\\.ru/rent/flat/\\d+/?$";
            Pattern pattern = Pattern.compile(regex);

            Set<String> uniqueLinks = new LinkedHashSet<>();
            for (String link : list) {
                Matcher matcher = pattern.matcher(link);
                if (matcher.matches()) {
                    uniqueLinks.add(link);
                }
            }

            Set<String> existingLinks = loadLinksFromFile();

            Set<String> newLinks = new LinkedHashSet<>(uniqueLinks);
            newLinks.removeAll(existingLinks);

            boolean isNewUser = isNewUser(userId);

            if (isNewUser) {
                for (String link : uniqueLinks) {
                    BOT.sendNotification(userId, link);
                }
                markUserAsProcessed(userId);
            } else {
                for (String link : newLinks) {
                    BOT.sendNotification(userId, link);
                }
            }

            saveLinksToFile(uniqueLinks);

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isNewUser(String userId) {
        Set<String> processedUsers = loadProcessedUsersFromFile();
        return !processedUsers.contains(userId);
    }

    private static void markUserAsProcessed(String userId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROCESSED_USERS_FILE, true))) {
            writer.write(userId);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка при добавлении пользователя в обработанные: " + e.getMessage());
        }
    }

    private static Set<String> loadProcessedUsersFromFile() {
        Set<String> users = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PROCESSED_USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл обработанных пользователей не найден. Будет создан новый.");
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла обработанных пользователей: " + e.getMessage());
        }
        return users;
    }

    private static Set<String> loadLinksFromFile() {
        Set<String> links = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LINKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                links.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл ссылок не найден. Будет создан новый.");
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла ссылок: " + e.getMessage());
        }
        return links;
    }

    private static void saveLinksToFile(Set<String> links) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LINKS_FILE))) {
            for (String link : links) {
                writer.write(link);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении ссылок в файл: " + e.getMessage());
        }
    }
}
