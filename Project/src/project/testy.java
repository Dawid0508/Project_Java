package project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class testy {

    public static void main(String[] args) {
        // Konfiguracja Log4j
        System.setProperty("log4j.configurationFile", "src/log4j2.xml");

        // Tworzenie obiektu Logging dla klasy Main
        Logging loggingMain = new Logging(Main.class);
        Logger loggerMain = loggingMain.getLogger();

        int number = 1;
        try {
            String targetUrl = "http://api.nbp.pl/api/exchangerates/tables/A/";

            // Sprawdzanie poprawności adresu URL
            if (!isValidUrl(targetUrl)) {
                loggingMain.logFatal("Invalid URL: ", targetUrl);
                return;
            }

            Connection connection = Jsoup.connect(targetUrl);
            Document doc = connection.ignoreContentType(true).get();

            // Pobranie zawartości HTML jako tekst
            String jsonString = doc.body().text();

            // Parsowanie danych JSON
            JSONArray jsonArray = new JSONArray(jsonString);

            // Pobranie pierwszego obiektu JSON (zakładając, że istnieje tylko jeden)
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            // Pobranie tablicy walut
            JSONArray ratesArray = jsonObject.getJSONArray("rates");

            // Przejście przez tablicę walut i logowanie nazw
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("nazwy_walut.txt"))) {
                for (int i = 0; i < ratesArray.length(); i++) {
                    JSONObject rateObject = ratesArray.getJSONObject(i);
                    String currencyName = rateObject.getString("currency");
                    loggingMain.logInfo(currencyName);
                    writer.write(currencyName);
                    writer.newLine();
                    number++;
                }
            } catch (IOException e) {
                loggingMain.logError("An error occurred while writing to the file.", e);
            }
        } catch (Exception e) {
            loggingMain.logError("An error occurred.", e);
        }
    }

    private static boolean isValidUrl(String url) {
        try {
            new java.net.URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

