package project;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String CHECK_URL3 = "http://api.nbp.pl/api/exchangerates/tables/C/";
    private static final String CHECK_URL2 = "http://api.nbp.pl/api/exchangerates/tables/A/last/7/";
    private static final String CHECK_URL1 = "http://api.nbp.pl/api/exchangerates/tables/A/";
    
    
    private static final int TIMEOUT_MS = 20000; // 20 sekund   

    public static void main(String[] args) {
        Logging loggingMain = new Logging(Main.class);
        	
        String targetUrl = "http://api.nbp.pl/api/exchangerates/tables/A/";
        String targetUrl7Days = "http://api.nbp.pl/api/exchangerates/tables/A/last/7/";
            
        // nowe pole do przechowywania linku z bid i ask
        String bidAskUrl = "http://api.nbp.pl/api/exchangerates/tables/C/";
        String bidAskFileName = "bid_ask.txt";
            
       try {
            
            if (!isValidUrl(targetUrl,logger)) {
                loggingMain.logFatal("Invalid URL: ", targetUrl);
                return;
            }
            
            if (!isValidUrl(targetUrl7Days, logger)) {
                loggingMain.logFatal("Invalid URL: ", targetUrl7Days);
                return;
            }
            
            if (!isValidUrl(bidAskUrl, logger)) {
                loggingMain.logFatal("Invalid URL: ", bidAskUrl);
                return;
            }
       }catch (Exception e) {
                loggingMain.logError("An error occurred while connecting to the server", e);
                logger.error("An error occurred", e);
            	
            }
                    
       try{
    	   if(isInternetAvailable(CHECK_URL1)) {
                List<Currency> currencies = downloadCurrencies(targetUrl);
                if(currencies.isEmpty()) {
                    logger.warn("No data found in the list");
                    
                }else {
                    printAndSaveCurrencies(currencies, "nazwy_walut.txt", loggingMain);
                    logger.debug("currencies info saved");
                }
    	   }
       }catch (Exception e) {
               loggingMain.logError("An error occurred while connecting to the server", e);
               logger.error("An error occurred", e);
            	
            }         
            
       try {
    	   if(isInternetAvailable(CHECK_URL2)) {
               List<Currency> currenciesLast7Days;
               currenciesLast7Days = downloadCurrenciesLast7Days(targetUrl7Days);
                   
               if (currenciesLast7Days.isEmpty()) {
                   logger.warn("No data found in the list");
               } else {
                   // Normalne przetwarzanie danych
                   printAndSaveCurrencies2(currenciesLast7Days, "kursy_waluty_last_7_days.txt", loggingMain);
                   logger.debug("7-days-history saved");
               }
    	   }
       }catch (Exception e) {
           loggingMain.logError("An error occurred while connecting to the server", e);
           logger.error("An error occurred", e);
       }

            
       try{
    	   if(isInternetAvailable(CHECK_URL3)) {
               // Pobieranie bid i ask i utwórz listę z informacjami
               List<BidAskInfo> bidAskInfoList = downloadBidAndAsk(bidAskUrl, loggingMain);
               if(bidAskInfoList.isEmpty()) {
                   logger.warn("No data found in the list");	
               }else {
                   // Zapis informacje o bid i ask do pliku
                   saveBidAndAsk(bidAskInfoList, bidAskFileName, loggingMain);
                   logger.debug("bid-ask info saved");
               }

    	   }
       }catch (Exception e) {
           loggingMain.logError("An error occurred while connecting to the server", e);
           logger.error("An error occurred", e);
       }
        
    }

    public static boolean isInternetAvailable(String CHECK_URL) {
        try {
            URL url = new URL(CHECK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.connect();

            // Sprawdź kod odpowiedzi HTTP, 200 oznacza sukces
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	logger.debug("Masz dostep do internetu");
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Timeout, brak połączenia, itp.
        	logger.fatal("Brak dostepu do internetu lub przekroczony czas oczekiwania dla URL:" + CHECK_URL);
            return false;
        }
        return false;
    }

       
    public static List<String> downloadCurrencyInfo(String url) {
        List<String> imageUrls = new ArrayList<>();

        try {
            // Nawiązanie połączenia z witryną internetową i pobranie zawartości HTML
            Document document = Jsoup.connect(url).get();

            // Używamy selektorów CSS do znalezienia wierszy z danymi o walutach
            Elements rows = document.select(".curr_table tbody tr");

            // Iterowanie przez wiersze i pobranie linków (img src)
            for (Element row : rows) {
                String imageUrl = row.select(".td").attr("img");

                // Dodanie linka do listy
                imageUrls.add(imageUrl);
            }

        } catch (IOException e) {
            logger.error("Błąd podczas pobierania informacji o walutach: " + e.getMessage());
        }

        return imageUrls;
    }
    

    private static List<Currency> downloadCurrencies(String url) {
        List<Currency> currencies = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(url);
            Document doc = connection.ignoreContentType(true).get();
            String jsonString = doc.body().text();
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray ratesArray = jsonObject.getJSONArray("rates");

            for (int i = 0; i < ratesArray.length(); i++) {
                JSONObject rateObject = ratesArray.getJSONObject(i);
                String currencyName = rateObject.getString("currency");
                String currencyCode = rateObject.getString("code");
                double currencyMidRate = rateObject.getDouble("mid");
                logger.debug(currencyName + currencyCode + currencyMidRate);

                currencies.add(new Currency(currencyCode, currencyName, currencyMidRate));
            }
        } catch (IOException | JSONException e) {
            logger.error("An error occurred", e);
        }
        return currencies;
    }

    private static List<Currency> downloadCurrenciesLast7Days(String url) {
        List<Currency> currenciesLast7Days = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(url);
            Document doc = connection.ignoreContentType(true).get();
            String jsonString = doc.body().text();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tableObject = jsonArray.getJSONObject(i);
                JSONArray ratesArray = tableObject.getJSONArray("rates");

                for (int j = 0; j < ratesArray.length(); j++) {
                    JSONObject rateObject = ratesArray.getJSONObject(j);

                    String currencyCode = rateObject.getString("code");
                    String currencyName = rateObject.getString("currency");
                    String date = rateObject.optString("effectiveDate", "N/A");// optString, aby uniknąć JSONException
                    //logger.info("date " + date);
                    
                    double currencyMidRate = rateObject.getDouble("mid");
                    
                    if (date.equals("N/A") && !currenciesLast7Days.isEmpty()) {
                        String previousDate = currenciesLast7Days.get(currenciesLast7Days.size() - 1).getDate();
                        date = previousDate;
                    }

                    currenciesLast7Days.add(new Currency(currencyCode, currencyName, date, currencyMidRate));
                }
            }
        } catch (IOException | JSONException e) {
            logger.error("An error occurred", e);
        }
        return currenciesLast7Days;
    }
    
    //pobieramy nazwy walut np 
    private static void printAndSaveCurrencies(List<Currency> currencies, String fileName, Logging logging) {
        PropertiesHandler propertiesHandler = new PropertiesHandler(fileName);

        Map<String, List<Currency>> currenciesMap = new HashMap<>();
        for (Currency currency : currencies) {
            currenciesMap.computeIfAbsent(currency.getCode(), k -> new ArrayList<>()).add(currency);
        }

        for (Map.Entry<String, List<Currency>> entry : currenciesMap.entrySet()) {
            logging.logInfo("Currency: " + entry.getKey());
            
            for (Currency currency : entry.getValue()) {
                logging.logInfo(currency.toString());
                
                // Sprawdzamy, czy dana linia już istnieje w pliku
                String line = currency.toString();
                if (!propertiesHandler.doesLineExist(line)) {
                    propertiesHandler.saveProperty(line);

                } else {
                    logger.info("Property already exists: " + line);
                }
            }
        }
    }
    
    //do zapisywania historii z 7 dni
    private static void printAndSaveCurrencies2(List<Currency> currencies, String fileName, Logging logging) {
        PropertiesHandler propertiesHandler = new PropertiesHandler(fileName);

        Map<String, List<Currency>> currenciesMap = new HashMap<>();
        for (Currency currency : currencies) {
            currenciesMap.computeIfAbsent(currency.getCode(), k -> new ArrayList<>()).add(currency);
        }

        for (Map.Entry<String, List<Currency>> entry : currenciesMap.entrySet()) {
            logging.logInfo("Currency: " + entry.getKey());
            
            if(!propertiesHandler.doesLineExist(entry.getKey())){
            	propertiesHandler.saveProperty("Currency: " + entry.getKey());
            
            
	            for (Currency currency : entry.getValue()) {
	                logging.logInfo(currency.toString());
	                
	                // Sprawdź, czy dana linia już istnieje w pliku
	                String line = currency.toString();
	                if (!propertiesHandler.doesLineExist(line)) {
	                    propertiesHandler.saveProperty(line);
	
	                } else {
	                    logger.info("Property already exists: " + line);
	                }
	            }
            }else {
            	logging.logInfo("Property already exists: " + entry.getKey());
            }
        }
    }
    private static List<BidAskInfo> downloadBidAndAsk(String url, Logging logging) {
        List<BidAskInfo> bidAskInfoList = new ArrayList<>();

        try {
            Connection connection = Jsoup.connect(url);
            Document doc = connection.ignoreContentType(true).get();
            String jsonString = doc.body().text();

            logging.logInfo("Received JSOn respond: " + jsonString);  

            JSONArray exchangeRatesTableArray = new JSONArray(jsonString);
            
            for (int i = 0; i < exchangeRatesTableArray.length(); i++) {
                JSONObject exchangeRatesTableObject = exchangeRatesTableArray.getJSONObject(i);
                JSONArray ratesArray = exchangeRatesTableObject.getJSONArray("rates");

                for (int j = 0; j < ratesArray.length(); j++) {
                    JSONObject rateObject = ratesArray.getJSONObject(j);

                    String code = rateObject.getString("code");
                    double bid = rateObject.getDouble("bid");
                    double ask = rateObject.getDouble("ask");

                    BidAskInfo bidAskInfo = new BidAskInfo(code, bid, ask);
                    bidAskInfoList.add(bidAskInfo);
                }
            }

            logging.logInfo("Bid and Ask information retrieved successfully.");
        } catch (IOException | JSONException e) {
            logging.logError("An error occurred while retrieving Bid and Ask information.", e);
        }

        return bidAskInfoList;
    }

    private static void saveBidAndAsk(List<BidAskInfo> bidAskInfoList, String fileName, Logging logging) {
            PropertiesHandler bidAskPropertiesHandler = new PropertiesHandler(fileName);

            for (BidAskInfo bidAskInfo : bidAskInfoList) {
                // Zapisz do pliku kod, bid i ask osobno
            	String line = "Code: " + bidAskInfo.getCode() + ", " + "Bid for " + bidAskInfo.getCode() + ": " + bidAskInfo.getBid() + ", " + "Ask for " + bidAskInfo.getCode() + ": " + bidAskInfo.getAsk();
                if(!bidAskPropertiesHandler.doesLineExist(line)) {
                	bidAskPropertiesHandler.saveProperty(line);
                    logging.logInfo("Bid and Ask saved to: " + fileName);
                }else {
                	logging.logInfo("property already saved: " + line);
                }
            	
            }
    }
    
    
    

    private static boolean isValidUrl(String url, Logger logger) {
        try {
            new java.net.URI(url).toURL();
            return true;
        } catch (Exception e) {
        	logger.error("Valid URl" + url);
            return false;
        }
    }


    // klasa przechowująca informacje o bid i ask
    public static class BidAskInfo {
        private String code;
        private double bid;
        private double ask;

        public BidAskInfo(String code, double bid, double ask) {
            this.code = code;
            this.bid = bid;
            this.ask = ask;
        }

        public String getCode() {
            return code;
        }

        public double getBid() {
            return bid;
        }

        public double getAsk() {
            return ask;
        }

        @Override
        public String toString() {
            return "Currency Code: " + code + ", Bid: " + bid + ", Ask: " + ask;
        }
    }


    //klasa przechowujaca informacje o walucie
    public static class Currency {
	    private String code;
	    private String name;
	    private double midRate;
	    private String date;
	    private double bid;
	    private double ask;
	
	    public Currency(String code, String name, double midRate) {
	        this.code = code;
	        this.name = name;
	        this.midRate = midRate;
	    }
	
	    public Currency(String code, double midRate, String date) {
	        this.code = code;
	        this.date = date;
	        this.midRate = midRate;
	    }
	    public Currency(String code, String name, String date, double midRate) {
	        this.code = code;
	        this.name = name;
	        this.date = date;
	        this.midRate = midRate;
	    }
	    public String getCode() {
	        return code;
	    }
	    public void setDate(String date) {
	        this.date = date;
	    }
	    public String getDate() {
	    	return date;
	    }
	 // nowy konstruktor, który zawiera również bid i ask
	    public Currency(String code, String name, double midRate, double bid, double ask) {
	        this.code = code;
	        this.name = name;
	        this.midRate = midRate;
	        this.bid = bid;
	        this.ask = ask;
	    }
	    
	    public double getBid() {
	        return bid;
	    }
	
	    public double getAsk() {
	        return ask;
	    }
	    public String getName() {
	    	return name;
	    }
	    public double getMidRate() {
	    	return midRate;
	    }
	    
	
	    @Override
	    public String toString() {
	        if (date != null) {
	            //return "Currency: " + code + ", Date: " + date + ", Mid Rate: " + midRate;
	            //return "Date: " + date + ", Mid Rate: " + midRate;
	            return " Mid Rate: " + midRate;
	        } else {
	            //return "Code: " + code + ", Name: " + name + ", Mid Rate: " + midRate;
	            //return "Date: " + date + ", Mid Rate: " + midRate;
	        	return "Currency: " + code + ", Name: " + name + ", Mid Rate: " + midRate;
	        }
	    }
    }
}