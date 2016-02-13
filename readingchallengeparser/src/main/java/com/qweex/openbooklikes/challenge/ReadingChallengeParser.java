package com.qweex.openbooklikes.challenge;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
java -cp .:/Users/notbryant/.gradle/caches/modules-2/files-2.1/org.jsoup/jsoup/1.8.3/65fd012581ded67bc20945d85c32b4598c3a9cf1/jsoup-1.8.3.jar ReadingChallengeParser
 */


public class ReadingChallengeParser {
    Document doc;
    public int total, current;
    public String year, userId;
    public String[] past;
    public double percentageOfYear;
    double percentageOfBooks;

    private static final String
            URL = "http://booklikes.com/apps/reading-challenge/%1s/%2s";

    public ReadingChallengeParser(String userId, String yearToLoad) throws IOException, NoSuchChallengeException {
        this.userId = userId;
        this.year = yearToLoad;
        doc = Jsoup
                .connect(url())
                .timeout(6000)
                .get();

        // Attempt to snag the past years
        try {
            Elements pastEls = doc.select(".challenge > .set-left > a[href^=\"http://booklikes.com/apps/reading-challenge/\"]");

            this.past = new String[pastEls.size() - 1];
            for(int i=0, j=0; i<pastEls.size(); i++) {
                Element element = pastEls.get(i);
                if(element.hasClass("nav-active")) {
                    if(this.year.length()==0)
                        this.year = element.text();
                    else if(!this.year.equals(element.text()))
                        throw new RuntimeException("This should be impossible: the year sought was not found (" + this.year + " vs " + element.text() + ")");
                }else
                    this.past[j++] = pastEls.get(i).text();
            }

            String[] x = doc.select(".challenge-dashboard > .set-right").first().text().split("[ /]");
            current = Integer.parseInt(x[0]);
            total = Integer.parseInt(x[1]);
        } catch(NullPointerException n) {
            throw new IOException("No such Reading Challenge exists");
        } catch(java.lang.NegativeArraySizeException na) {
            throw new NoSuchChallengeException("No such Reading Challenge exists");
        }

        percentageOfYear = 100.0*(365-daysRemaining()) / 365;
        percentageOfBooks = 100.0*current / total;
    }

    public ReadingChallengeParser(String userId) throws IOException {
        this(userId, Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
    }

    public String url() {
        return String.format(URL, userId, year);
    }

    public double daysRemaining() {
        Calendar start = Calendar.getInstance();
        Calendar end = new GregorianCalendar(start.get(Calendar.YEAR), 12 -1, 31);


        long secs = (end.getTime().getTime() - start.getTime().getTime()) / 1000;
        double hours = secs / 3600;
        secs = secs % 3600;
        double mins = secs / 60;
        secs = secs % 60;
        return hours / 24;
    }

    public double perDay() {
        return daysRemaining() * 1.0 / (total - current);
    }

    public double behind() {
        return (percentageOfYear-percentageOfBooks)/100 * total;
    }

    public static class NoSuchChallengeException extends RuntimeException {
        public NoSuchChallengeException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        System.out.println("Fetching the ");
        try {
            ReadingChallengeParser r = new ReadingChallengeParser("69841");
            System.out.println("Current year " + r.year + ": " + r.current + "/" + r.total);
            System.out.print("Past years: ");
            for(String s : r.past)
                System.out.print(s + " ");
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
