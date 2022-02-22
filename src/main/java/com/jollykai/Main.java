package com.jollykai;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static String USER_NAME = "***";  // GMail username (just the part before "@gmail.com")
    private final static String PASSWORD = "***"; // GMail password
    private final static String RECIPIENT = "***";

    private final static String URL = "https://www.parkrun.ru/petergofaleksandriysky/results/latestresults/";
    private final static String OUTPUT_FILE = "results.txt";

    public static void main(String[] args) {

        //Parsing site to get volunteerList
        List<Volunteer> volunteerList = countVolunteers(new ArrayList<>());

        //Write info in file
        try (OutputStreamWriter writeInFile =
                     new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE), StandardCharsets.UTF_8)) {

            writeInFile.write("Волонтеров в забеге: " + (volunteerList.size() - 1) + "\n");
            for (Volunteer volunteer : volunteerList) {
                writeInFile.write("\n" + volunteer.toString());
                writeInFile.write(setAchievement(volunteer.getVolunteeringCounter()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    /*
        //send email
        String from = USER_NAME;
        String pass = PASSWORD;
        String[] to = {RECIPIENT}; // list of recipient email addresses
        String subject = "Отчет по волонтерам Parkrun Петергоф";

        Reader fr = new FileReader(file);
        StringBuilder sb = new StringBuilder();
        int x = fr.read();
        while (x != -1) {
            sb.append((char)x);
            x = fr.read();
        }

        String body = sb.toString();
        sendFromGMail(from, pass, to, subject, body);
    }

    private static void sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for (int i = 0; i < to.length; i++) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for (int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
     */
    }

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
            if (!link.attr("href").replaceAll("[^0-9]", "").equals(""))
                volunteerList.add(new Volunteer(
                        Integer.parseInt(link.attr("href").replaceAll("[^0-9]", "")),
                        link.text()));
        }
        return getCounter(volunteerList);
    }

    private static List<Volunteer> getCounter(List<Volunteer> volunteerList) {

        for (Volunteer volunteer : volunteerList) {

            Document volunteerProfilePage = null;

            try {
                volunteerProfilePage = Jsoup.connect("https://www.parkrun.ru/parkrunner/" + volunteer.getID()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert volunteerProfilePage != null;
            volunteer.setVolunteeringCounter(Integer.parseInt(volunteerProfilePage.select("td:nth-child(2) > strong").text()));

        }
        return volunteerList;
    }

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
}
