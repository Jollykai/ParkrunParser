package com.jollykai.parkrunparser.parser.impl;

import com.jollykai.parkrunparser.StringUtils;
import com.jollykai.parkrunparser.parser.ParkunParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@PropertySource("classpath:parkun.properties")
public class DefaultParkunParser implements ParkunParser {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultParkunParser.class);

    private static String USER_NAME = "***";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "***"; // GMail password
    private static String RECIPIENT = "***";

    @Value("${parkrun.url}")
    private String endpoint;
    @Value("${parkrun.output.file}")
    private String outputFile;

    @Override
    public void parse() throws IOException {
        LOG.debug("Starting a task");
        Document document = Jsoup.connect(endpoint).get();
        Elements user = document.select("p:nth-child(2) > a");
        File file = new File(outputFile);
        FileWriter fw = new FileWriter(file);

        List<Integer> volounterID = new ArrayList<>();
        List<String> volounterNames = new ArrayList<>();
        List<Integer> volounterCount = new ArrayList<>();

        someFunc(user, volounterID, volounterNames, fw);

        //take num of vol
        for (int num : volounterID) {
            StringBuilder volCount = new StringBuilder();
            Document volounterPage = Jsoup.connect("https://www.parkrun.ru/parkrunner/" + num).get();
            Elements volounterCounter = volounterPage.select("td:nth-child(2) > strong");
            for (char ch : volounterCounter.toString().toCharArray()) {
                if (Character.isDigit(ch)) {
                    volCount.append(ch);
                }
            }
            volounterCount.add(Integer.parseInt(volCount.toString()));
            fw.flush();

        }
        // write in file
        fw.write("Волонтёров в забеге: " + (user.size() - 1) + "\n\n");

        for (int i = 0; i < volounterID.size(); i++) {
            String res = "Волонтёр - " + volounterNames.get(i) + ". Волонтёрств всего: " + volounterCount.get(i);
            fw.write(res);
            fw.write(getMessageByCount(volounterCount.get(i)));
            if (i != volounterID.size() - 1) fw.write("\n");
        }
        fw.close();
        //send email
        String from = USER_NAME;
        String pass = PASSWORD;
        String[] to = {RECIPIENT}; // list of recipient email addresses
        String subject = "Отчёт по волонтёрам Parkrun Петергоф";

        Reader fr = new FileReader(file);
        StringBuilder sb = new StringBuilder();
        int x = fr.read();
        while (x != -1) {
            sb.append((char) x);
            x = fr.read();
        }

        String body = sb.toString();
        sendFromGMail(from, pass, to, subject, body);
    }

    private void someFunc(Elements user,  List<Integer> volounterID,List<String> volounterNames,FileWriter fw ){
        user.forEach(u -> {
            StringBuilder id = new StringBuilder();
            StringBuilder nm = new StringBuilder();
            boolean closedA = false;
            for (char ch : u.toString().toCharArray()) {
                if (Character.isDigit(ch)) {
                    id.append(ch);
                }
                if (ch == '>') {
                    closedA = true;
                } else if (ch == '<') {
                    closedA = false;
                }
                if ((closedA) & (ch != '>') & (ch != '<')) {
                    nm.append(ch);
                }
            }
            StringUtils.mayBeInteger(id.toString()).ifPresent(volounterID::add);
            volounterNames.add(nm.toString());
            try {
                fw.flush();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private static String getMessageByCount(int i) {
        switch (i) {
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
        } catch (MessagingException ae) {
            LOG.error(ae.getMessage(), ae);
        }
    }
}
