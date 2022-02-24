package com.jollykai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    private final static String URL = "https://www.parkrun.ru/petergofaleksandriysky/results/latestresults/";
    private final static String OUTPUT_FILE = "results.txt";
    private final static String CONFIG_FILE = "config.json";
    private final static String SUBJECT = "Отчет по волонтерам Parkrun Петергоф Александрийский";

    private static String USER_NAME = "";
    private static String PASSWORD = "";
    private static String RECIPIENT = "";

    public static void main(String[] args) {

        //Parsing site to get data for volunteerList
        List<Volunteer> volunteerList = countVolunteers(new ArrayList<>());

        //Write parsed data in local file
        saveDataToFile(volunteerList);

        //Create config file
        configFileExistCheck();

        //Read config data
        readConfigFile();

        //Send mail
        sendMail(USER_NAME, PASSWORD, RECIPIENT);

    }

    /**
     * Parse HTML, saves $ID and $names in List, and call getCounter(List<Obj> List) to collect other data.
     *
     * @param volunteerList empty List
     * @return List with all collected data received from getCounter(List<Obj> List)
     * @see #getCounter(List)
     */
    private static List<Volunteer> countVolunteers(List<Volunteer> volunteerList) {

        Document runningResultsPage = null;

        try {
            runningResultsPage = Jsoup.connect(URL)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();

        } catch (IOException e) {
            e.printStackTrace();
        }

        assert runningResultsPage != null;
        Elements user = runningResultsPage.select("p:nth-child(2) > a");

        for (Element link : user) {

            if (!link.attr("href").replaceAll("[^0-9]", "").equals("")) {

                volunteerList.add(new Volunteer(
                        Integer.parseInt(link.attr("href").replaceAll("[^0-9]", "")),
                        link.text()));

            }

        }
        return getCounter(volunteerList);
    }

    /**
     * Parse HTML and saves $volunteeringCounter of Objects in List.
     *
     * @param volunteerList List of Object containing $ID, which uses to get required URL
     * @return List with all fields filled
     * @see #countVolunteers(List)
     */
    private static List<Volunteer> getCounter(List<Volunteer> volunteerList) {

        for (Volunteer volunteer : volunteerList) {

            Document volunteerProfilePage = null;

            try {
                volunteerProfilePage = Jsoup.connect("https://www.parkrun.ru/parkrunner/" + volunteer.getID()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert volunteerProfilePage != null;
            volunteer.setVolunteeringCounter(
                    Integer.parseInt(volunteerProfilePage.select("td:nth-child(2) > strong").text()));

        }
        return volunteerList;
    }

    /**
     * Checks Counters, for achievement and saves data to local file.
     * @param volunteerList List of Object containing $volunteeringCounter,
     *                      what compares whether achievements have been received
     * @see #setAchievement(int)
     */
    private static void saveDataToFile(List<Volunteer> volunteerList) {

        try (OutputStreamWriter writeInFile =
                     new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE), StandardCharsets.UTF_8)) {

            writeInFile.write(SUBJECT + "\n\n");
            writeInFile.write("Волонтеров в забеге: " + (volunteerList.size() - 1) + "\n");

            for (Volunteer volunteer : volunteerList) {
                writeInFile.write("\n" + volunteer.toString());
                writeInFile.write(setAchievement(volunteer.getVolunteeringCounter()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks, is current number gives achievement.
     *
     * @param volunteeringCounter Times of volunteering
     * @return String with achievement name
     * @see #saveDataToFile(List)
     */
    private static String setAchievement(int volunteeringCounter) {

        switch (volunteeringCounter) {

            case 1:
                return " - Первое волонтерство!";
            case 10:
                return " - Теперь в клубе 10!* Только если возраст меньше 18! Проверить";
            case 25:
                return " - Теперь в клубе 25!";
            case 50:
                return " - Теперь в клубе 50!";
            case 100:
                return " - Теперь в клубе 100!";
            case 250:
                return " - Теперь в клубе 250!";
            case 500:
                return " - Теперь в клубе 500!";

        }
        return "";
    }

    /**
     * Creates config file from user input data in case config file not exist.
     */
    private static void configFileExistCheck() {

        File file = new File(CONFIG_FILE);

        String from;
        String password;
        String to;

        if (!file.exists()) {

            System.out.println("Файл конфигурации - \"" + CONFIG_FILE + "\", не обнаружен! " +
                    "Введите данные авторизации\n");
            Scanner userInput = new Scanner(System.in);

            System.out.print("Gmail Логин (без @gmail.com): ");
            from = userInput.nextLine().trim();

            System.out.print("Gmail Пароль: ");
            password = userInput.nextLine().trim();

            System.out.print("E-mail ардес получателя отчетов: ");
            to = userInput.nextLine().trim();
            userInput.close();

            JSONObject gmailSettings = new JSONObject();
            gmailSettings.put("Login", from);
            gmailSettings.put("Password", password);

            JSONObject config = new JSONObject();
            config.put("Gmail Settings", gmailSettings);
            config.put("Recipient", to);

            try (FileWriter createConfigFile = new FileWriter(CONFIG_FILE)) {
                createConfigFile.write(config.toString(1));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Read auth data for sending mail.
     */
    private static void readConfigFile() {

        ObjectMapper mapper = new ObjectMapper();

        try {

            String configString = Files.readString(Path.of(CONFIG_FILE), StandardCharsets.UTF_8);
            JsonNode gmailSettings = mapper.readTree(configString).get("Gmail Settings");

            USER_NAME = mapper.readTree(String.valueOf(gmailSettings)).get("Login").textValue();
            PASSWORD = mapper.readTree(String.valueOf(gmailSettings)).get("Password").textValue();
            RECIPIENT = mapper.readTree(configString).get("Recipient").textValue();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Send e-mail with data from file with results.
     *
     * @param from    Login for smtp server auth
     * @param password PWD for smtp server auth
     * @param to       E-mail of recipient
     * @see #readConfigFile()
     */
    private static void sendMail(String from, String password, String to) {

        Properties props = System.getProperties();
        String host = "smtp.gmail.com";

        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {

            String letterBody = Files.readString(Path.of(OUTPUT_FILE), StandardCharsets.UTF_8);

            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(Main.SUBJECT);
            message.setText(letterBody);

            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

        } catch (Exception messagingException) {
            messagingException.printStackTrace();
        }

    }

}