package com.qweex.openbooklikes.challenge;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Calendar;

/*
java -cp .:/Users/notbryant/.gradle/caches/modules-2/files-2.1/org.jsoup/jsoup/1.8.3/65fd012581ded67bc20945d85c32b4598c3a9cf1/jsoup-1.8.3.jar ReadingChallengeParser
 */


public class ReadingChallengeParser {
    Document doc;
    public int total, current;


    private static final String
            URL = "http://booklikes.com/widget/readingchallenge?id=%1s&year=%2s";


    public ReadingChallengeParser(String userId, int year) throws IOException {
        doc = Jsoup.connect(String.format(URL, userId, year)).get();

        current = Integer.parseInt(doc.select(".info > span:nth-of-type(1)").first().text());
        total = Integer.parseInt(doc.select(".info > span:nth-of-type(2)").first().text());
    }

    public ReadingChallengeParser(String userId) throws IOException {
        this(userId, Calendar.getInstance().get(Calendar.YEAR));
    }

    public ReadingChallengeParser(int userId) throws IOException {
        this(Integer.toString(userId));
    }

    public ReadingChallengeParser(int userId, int year) throws IOException {
        this(Integer.toString(userId), year);
    }

    //TODO: Fetch what years are available from
    //  nav.set-mb-20.set-left > a
    // or just
    //  nav
    //URL = "http://booklikes.com/apps/reading-challenge/%1s/%2s",

    public static void main(String[] args) {
        System.out.println("ADSDSADAS");
        try {
            ReadingChallengeParser r = new ReadingChallengeParser("69841");
            System.out.println(r.current + "/" + r.total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
