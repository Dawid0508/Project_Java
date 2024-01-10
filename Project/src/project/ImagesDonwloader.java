package project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import project.Main.Currency;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;


public class ImagesDonwloader {
    private static final Logger LOGGER = LogManager.getLogger(ImagesDonwloader.class);
    private static final String CHECK_URL = "https://www.kantor-exchange.pl/kursy-walut-krakow/";
    private static final int TIMEOUT_MS = 30000; // 30 sekund


    public static void main(String[] args) {
        
    	
    	try {
            // Wywołanie funkcji pobierania informacji o walutach
        	ImagesDonwloader downloader = new ImagesDonwloader();
            isValidUrl(CHECK_URL, LOGGER);
            
    	       try{
    	    	   if(isInternetAvailable(CHECK_URL)) {
    	               List<CurrencyInfo> currencyInfoList = downloadCurrencyInfo(CHECK_URL);           
    	                
    	               if(currencyInfoList.isEmpty()) {
    	                    LOGGER.warn("No data found in the list");	
    	                }else {
    	                	saveToProperties(currencyInfoList, "images.txt");
    	                	LOGGER.debug("images of currencies saved properly");
    	                	
    	                	
    	                	// Wyświetlanie pobranych informacji
    	                    for (CurrencyInfo currencyInfo : currencyInfoList) {
    	                        LOGGER.info("Code: " + currencyInfo.getCode() + ", Image URL: " + currencyInfo.getImageUrl());
    	                     }
    	                }

    	    	   }
    	       }catch (Exception e) {
    	               LOGGER.error("An error occurred", e);
    	            	
  
    	       }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("error during downloading images's URLS");
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
            	LOGGER.debug("Masz dostep do internetu");
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Timeout, brak połączenia, itp.
        	LOGGER.fatal("Brak dostepu do internetu lub przekroczony czas oczekiwania dla URL:" + CHECK_URL);
            return false;
        }
        return false;
    }

    
    private static boolean isValidUrl(String url, Logger logger) {
        try {
            new java.net.URI(url).toURL();
            return true;
        } catch (Exception e) {
        	logger.fatal("Valid URl" + url);
            return false;
        }
    }

    public static List<CurrencyInfo> downloadCurrencyInfo(String url) {
        List<CurrencyInfo> currencyInfoList = new ArrayList<>();

        try {
            // Nawiązanie połączenia z witryną internetową i pobranie zawartości HTML
        	System.out.println("Connecting to URL: " + url);
        	Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            LOGGER.error("HTML content retrieved successfully.");

            // Użyj selektorów CSS do znalezienia wierszy z danymi o walutach
            Elements rows = document.select(".card-header");
            LOGGER.error(rows);

            // Iteruj przez wiersze i pobierz linki (img src) oraz kod waluty (tekst h2)
            for (Element row : rows) {
                String imageUrl = row.select(".crypto-img").attr("src");
                LOGGER.info(imageUrl); // to dziala 
                String currencyCode = row.select("h2").text();
                
                //usuwa 100 z kodu waluty
                currencyCode = currencyCode.substring(4);
                LOGGER.info(imageUrl);

                // Dodaje informacje o walucie do listy
                currencyInfoList.add(new CurrencyInfo(currencyCode,  imageUrl));
            }

        } catch (IOException e) {
            LOGGER.error("Błąd podczas pobierania informacji o walutach: " + e.getMessage());
        }

        return currencyInfoList;
    }
    
    public static void saveToProperties(List<CurrencyInfo> data, String fileName) {
        PropertiesHandler propertiesHandler = new PropertiesHandler(fileName);
        String lin1 = "Code: SGD, Image URL: https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/Flag_of_Singapore.svg/105px-Flag_of_Singapore.svg.png";
        String lin2 = "Code: JPY, Image URL: https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Flag_of_Japan.svg/105px-Flag_of_Japan.svg.png";
        
        if(!propertiesHandler.doesLineExist(lin1)) {
        	propertiesHandler.saveProperty(lin1);
        }
        
        if(!propertiesHandler.doesLineExist(lin2)) {
        	propertiesHandler.saveProperty(lin2);
        }
        
        
        for (CurrencyInfo currencyInfo : data) {
            String propertyValue = "Code: " + currencyInfo.getCode() + ", Image URL: " + currencyInfo.getImageUrl();
            
            //spr czy linijka juz istnieje w pliku
            if(!propertiesHandler.doesLineExist(propertyValue)) {
            	
            
            	propertiesHandler.saveProperty(propertyValue);
            }else {
            	LOGGER.info("adres url dla waluty: " + currencyInfo.getCode() + "juz istnieje");
            }
        
        }
    }


	public static class CurrencyInfo {
		private String code;
		private String imageUrl;

		public CurrencyInfo(String code, String imageUrl) {
			this.code = code;
			this.imageUrl = imageUrl;
	    }
	
	    public String getCode() {
	        return code;
	    }
	
	    public String getImageUrl() {
	        return imageUrl;
	    }
	}
}
