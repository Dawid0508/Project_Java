package project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Main {
	
    public static void main(String[] args) {
        //Konfiguracja Log4j
    	System.setProperty("log4j.configurationFile","src/log4j2.xml");
    	PropertiesHandler propertiesHandler = new PropertiesHandler("nazwy_walut.txt");
    	//String fileName = "nazwy_walut.txt";
    	

    	
    	
    	//tworzenie obiektu Logging dla klasy Main
    	Logging loggingMain = new Logging(Main.class);
    	Logger loggerMain = loggingMain.getLogger();
    	
    	//List<String> currencyNameList = new ArrayList<>();   //lista przechowujaca nazwy walut
    	//saveListToFile("nazwy_walut.txt", currencyNameList);    	
    	int number = 1;
        try {
        	String targetUrl = "http://api.nbp.pl/api/exchangerates/tables/A/";
            
        	//sprawdzanie poprawnosci adresu URL
        	if(!isValidUrl(targetUrl)) {
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
            System.out.println(jsonObject);
            
            // Pobranie tablicy walut
            JSONArray ratesArray = jsonObject.getJSONArray("rates");

            // Przejście przez tablicę walut i logowanie nazw
            for (int i = 0; i < ratesArray.length(); i++) {
                JSONObject rateObject = ratesArray.getJSONObject(i);
                String currencyName = rateObject.getString("currency");
                String currencyCode = rateObject.getString("code");
                double currencyMidRate = rateObject.getDouble("mid");
                
                loggingMain.logInfo("Code: " + currencyCode + " , Name: "+ currencyName + " , Mid Rate: " + currencyMidRate);
                propertiesHandler.saveProperty(number, "Code: " +  currencyCode + ",Name: " + currencyName + ", Mida Rate: " + currencyMidRate);
                number++;
                
                //currencyNameList.add(currencyName);
                
            }
        } catch (Exception e) {
            loggingMain.logError("An error occurred.", e);
            //loggingMain.logDebug("ups" , e.printStackTrace());
        }
        
    }
    
    private static boolean isValidUrl(String url) {
    	try {
    		new java.net.URI(url).toURL();
    		return true;
    	}catch(Exception e) {
    		return false;
    	}
    }
    
}

