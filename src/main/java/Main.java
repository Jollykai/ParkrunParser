import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class Main {

    private static String USER_NAME = "***";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "***"; // GMail password
    private static String RECIPIENT = "***";

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect("https://www.parkrun.ru/petergofaleksandriysky/results/latestresults/").get();
        Elements user = document.select("p:nth-child(2) > a");
        File file = new File("results.txt");
//        file.createNewFile();
        FileWriter fw = new FileWriter(file);

        List<Integer> volounterID = new ArrayList<>();
        List<String> volounterNames = new ArrayList<>();
        List<Integer> volounterCount = new ArrayList<>();

        for (int x = 0; x < user.size() - 1; x++) {
//            fw.write(user.get(x).toString(),0,user.get(x).toString().length());
//            if (x != user.size() - 2) fw.write("\n");
            // take id
            StringBuilder id = new StringBuilder();
            StringBuilder nm = new StringBuilder();
            boolean closedA = false;
            for (char ch : user.get(x).toString().toCharArray()) {
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
            volounterID.add(Integer.parseInt(id.toString()));
            volounterNames.add(nm.toString());
            fw.flush();
        }


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
        System.out.println();
        fw.write("Волонтеров в забеге: " + (user.size() - 1) + "\n\n");

        for (int i = 0; i < volounterID.size(); i++) {

            String res = "Волонтер - " + volounterNames.get(i) + ". Волонтерств всего: " + volounterCount.get(i);
            fw.write(res);
            switch (volounterCount.get(i)) {
                case 10: {
                    fw.write(" - Теперь в клубе 10!* Только если возраст меньше 18! Проверить");
                    break;
                }
                case 25: {
                    fw.write(" - Теперь в клубе 25!");
                    break;
                }
                case 50: {
                    fw.write(" - Теперь в клубе 50!");
                    break;
                }
                case 100: {
                    fw.write(" - Теперь в клубе 100!");
                    break;
                }
                case 250: {
                    fw.write(" - Теперь в клубе 250!");
                    break;
                }
                case 500: {
                    fw.write(" - Теперь в клубе 500!");
                    break;
                }
            }
            if (i != volounterID.size() - 1) fw.write("\n");
//            System.out.println(res);
        }
        fw.close();
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
    }
}