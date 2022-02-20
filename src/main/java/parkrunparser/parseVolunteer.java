package parkrunparser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class parseVolunteer {
//    private static String RESULTS_URL = "https://www.parkrun.ru/petergofaleksandriysky/results/latestresults/";

    public static void getList() throws IOException {

        String url = "https://www.parkrun.ru/petergofaleksandriysky/results/latestresults/";

        String html =
//                Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
                Jsoup.connect(url)
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get()
                .html();

        System.out.println(html);
//
//        System.out.println(document);
//        Elements user = document.select("p:nth-child(2) > a");
//        for (Element el : user) {
//            System.out.println(el.text());
        }
    }
