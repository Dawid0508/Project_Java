package project;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.geom.Line2D;

// zebr te zdjecia zaladowac
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class CurrencyApp extends JFrame {
    private static final Logger logger = LogManager.getLogger(CurrencyApp.class);
    private static final int TIMEOUT_MS = 20000; // 20 sekund
    private static final String CHECK_URL = "https://img.wprost.pl/img/euro-i-dolar/57/c9/5912f8fe706bcae5c75d78a44c92.jpeg";
	
	//this.imageUrls = loadImageUrls();
    private List<String> currencyNameList;
    private JList<String> currencyList;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private Map<String, String> currencyInfoMap;

    public CurrencyApp(List<String> currencies) {
        this.currencyNameList = currencies;
        this.currencyInfoMap = loadCurrencyInfo();
        initComponents();
        setResizable(false);
        setLocationRelativeTo(null);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent e) {
                setLocationRelativeTo(null);
            }
        });
    }
    //do sprawdzania czy jest połaczenie z internetem
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
    
    
    private void initComponents() {
    		
        JPanel upperPanel = new JPanel(new FlowLayout());
        JPanel lowerPanel = new JPanel(new BorderLayout());

        //Przycisk 1 z strzalka w lewo
        //ImageIcon bitcoinIcon = loadImageFromURL("https://e7.pngegg.com/pngimages/465/657/png-clipart-money-banknote-currency-money-saving-cash-thumbnail.png");
        ImageIcon bitcoinIcon = loadImageFromURL("https://img.wprost.pl/img/euro-i-dolar/57/c9/5912f8fe706bcae5c75d78a44c92.jpeg");
        //ImageIcon bitcoinIcon = loadImageFromURL("https://cdn4.vectorstock.com/i/1000x1000/99/88/stock-market-graph-or-forex-trading-chart-vector-26119988.jpg");
        
        JLabel bitcoinLabel = new JLabel(bitcoinIcon);
        upperPanel.add(bitcoinLabel);
        //dodanie zdjecia kursu
        //ImageIcon courseImage = loadImageFromURL("https://i.gremicdn.pl/image/free/fff54fee764866c9c31b5e17b70450b0/");
        ImageIcon courseImage = loadImageFromURL("https://cdn4.vectorstock.com/i/1000x1000/99/88/stock-market-graph-or-forex-trading-chart-vector-26119988.jpg");
        
        
        
        //dodanie przyciskow 
        JButton button1 = new JButton("Poprzednia waluta");
        JButton button2 = new JButton("Lista Walut");
        JButton button3 = new JButton("Nastepna waluta");

        button1.addActionListener(e -> {
            int selectedIndex = currencyList.getSelectedIndex();
            int listSize = currencyList.getModel().getSize();

            if (selectedIndex > 0) {
                currencyList.setSelectedIndex(selectedIndex - 1);
                logger.info("selected currency: " + currencyList.getSelectedValue());
            } else {
                currencyList.setSelectedIndex(listSize - 1);
                logger.info("selected currency: " + currencyList.getSelectedValue());
            }
        });

        button2.addActionListener(e -> {
            cardLayout.show(cardPanel, "LIST");
            logger.info("selected list");
        });

        button3.addActionListener(e -> {
            int selectedIndex = currencyList.getSelectedIndex();
            int listSize = currencyList.getModel().getSize();

            if (selectedIndex < listSize - 1) {
                currencyList.setSelectedIndex(selectedIndex + 1);
                logger.info("selected currency: " + currencyList.getSelectedValue());
            } else {
                currencyList.setSelectedIndex(0);
                logger.info("selected currency: " + currencyList.getSelectedValue());
            }
        });

        upperPanel.add(button1);
        upperPanel.add(button2);
        upperPanel.add(button3);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Dodanie panelu z obrazem
        if(isInternetAvailable(CHECK_URL)) {
        	JPanel imagePanel = createImagePanel(courseImage);
        	cardPanel.add(imagePanel, "IMAGE");
        }
        //panel z lista
        JPanel listPanel = createListPanel();
        cardPanel.add(listPanel, "LIST");
        
        Map<String, String> imageUrls = loadImageUrls();

        for (String currency : currencyNameList) {
            
        	JPanel imageDisplayPanel = createImageDisplayPanel(currency, imageUrls); 
            cardPanel.add(imageDisplayPanel, "IMAGE_DISPLAY_" + currency);
        	
        	JPanel currencyPanel = createCurrencyPanel(currency);
            cardPanel.add(currencyPanel, currency);

            JPanel historyPanel = createHistoryPanel(currency);
            cardPanel.add(historyPanel, "HISTORY_" + currency);
        }

        //stworzenie dwoch paneli
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(upperPanel, BorderLayout.NORTH);
        mainPanel.add(lowerPanel, BorderLayout.CENTER);

        lowerPanel.add(cardPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        setTitle("                                      CurrencyApp");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
    }

    private JPanel createImageDisplayPanel(String currency, Map<String, String> imageUrls) {
        JPanel imageDisplayPanel = new JPanel(new BorderLayout());
        imageDisplayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        //imageDisplayPanel.setBackground(Color.WHITE);
        imageDisplayPanel.setPreferredSize(new Dimension(250,250));
        
        Color newBackgroundColor = new Color(211, 221, 211); // Jasny zielony
        imageDisplayPanel.setBackground(newBackgroundColor);

        String imageUrl = imageUrls.get(currency);
        logger.info("Image URL for: " + currency  + " ," + imageUrl);
        
        

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageIcon icon = loadImageFromURL(imageUrl);
           
            if (icon != null) {
                JLabel imageLabel = new JLabel(icon);
                imageDisplayPanel.add(imageLabel, BorderLayout.CENTER);
                
                logger.debug(imageDisplayPanel);

                imageDisplayPanel.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        cardLayout.show(cardPanel, "HISTORY_" + currency);
                        logger.info("switching to historyPanel");
                    }
                });
            } else {
                logger.error("Error creating ImageIcon from URL: " + imageUrl);
                showNoImageAvailableMessage(imageDisplayPanel);
            }
        }
        logger.debug("imageDisplayPanel created");
        return imageDisplayPanel;
    }


    private void showNoImageAvailableMessage(JPanel imageDisplayPanel) {
        JLabel messageLabel = new JLabel("Brak dostępnego obrazu dla tej waluty");
        messageLabel.setHorizontalAlignment(JLabel.CENTER);

	}

    //odczyt adresow Url z pliku tekstowego images.txt
	private static Map<String, String> loadImageUrls() {
        Map<String, String> imageUrls = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("images.txt"));
            if(lines.isEmpty()) {
            	logger.warn("file images.txt is blank");
            }

            for (String line : lines) {
                String[] parts = line.split(", ");
                if (parts.length >= 2) {
                    // Znajdowanie fragmentow, ktore zawierają kod waluty i adres URL
                    String currencyPart = findValue(parts[0]);
                    logger.info("waluta " + currencyPart);
                    String imageUrlPart = findValue(parts[1]);
                    logger.info("image url" + imageUrlPart);

                    // Dodawanie do mapy, jeśli oba fragmenty są niepuste
                    if (!currencyPart.isEmpty() && !imageUrlPart.isEmpty()) {
                        logger.debug("pomyslne pobranie adresu URL dla waluty " + currencyPart + imageUrlPart);
                        imageUrls.put(currencyPart, imageUrlPart);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error loading image URLs from file");
            e.printStackTrace();
        }
        
        return imageUrls;
        
    }
    
    // Metoda do znajdowania wartości po ":" i ewentualnym obcięciu początkowych fragmentów
    private static String findValue(String input) {
        int colonIndex = input.indexOf(":");
        return colonIndex != -1 ? input.substring(colonIndex + 1).trim() : input.trim();
    }
    
    //wczytywanie i przetwarzanie obrazu z url 
    private ImageIcon loadImageFromURL(String imageUrl) {
        try {
            java.net.URL url = new java.net.URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            
            
            // Sprawdzenie, czy obraz został prawidłowo wczytany
            if (image != null) {
                // Skalowanie obrazu do nowych wymiarów
                Image scaledImage = image.getScaledInstance(250, 225, Image.SCALE_SMOOTH);
                
                // tworzenie  obrazu z przezroczystością
                BufferedImage transparentImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = transparentImage.createGraphics();
                
                //dodanie tła
                //Color backgroundColor = new Color(255,255, 0);
                //g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, 200, 200);
                
                //dodawanie skalowanego obrazu
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
                
                logger.info("image loaded corectly");
                return new ImageIcon(transparentImage);
                
            } else {
                logger.error("Error loading image from URL: " + imageUrl);
                return new ImageIcon(); // Return an empty image icon if loading fails
            }
        } catch (IOException e) {
            logger.error("Error loading image from URL: " + imageUrl);
            return new ImageIcon(); // Return an empty image icon if loading fails
        }
    }

    //tworzenie panelu ze zdjeciem na poczatkowym panelu
    private JPanel createImagePanel(ImageIcon imageIcon) {
        JPanel imagePanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel(imageIcon);
        
        
        if(isInternetAvailable(CHECK_URL)) {        	
        	imagePanel.add(imageLabel, BorderLayout.CENTER);
        }
        
        Color newBackgroundColor = new Color(173, 216, 230); // Nowy kolor tła dla zdjecia na samym poczatku
        imagePanel.setBackground(newBackgroundColor);

        //obsługę kliknięcia na obraz
        imagePanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	logger.info("lista walut");
                cardLayout.show(cardPanel, "LIST"); // Przełącz do panelu z listą walut
            }
        });
        
     // preferowany rozmiar dla panelu obrazu
        imagePanel.setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
        logger.debug("imagePanel created");
        return imagePanel;
    }
    

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel();
        currencyList = new JList<>(currencyNameList.toArray(new String[0]));
        JScrollPane listScrollPane = new JScrollPane(currencyList);

        //prefered width for the scroll pane
        listScrollPane.setPreferredSize(new Dimension(200, 220));
        
        Color newBackgroundColor = new Color(173, 216, 230); // Nowy kolor tła (czerwony)
        listPanel.setBackground(newBackgroundColor);

        //wysrodkowanie scrollPane
        listScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        listPanel.add(listScrollPane, BorderLayout.CENTER);

        currencyList.addListSelectionListener(e -> {
            String selectedCurrency = currencyList.getSelectedValue();
            if (selectedCurrency != null) {
                cardLayout.show(cardPanel, selectedCurrency);
               logger.info("Currency " + selectedCurrency + " selected");
            }
        });

        listPanel.setPreferredSize(new Dimension(200, 200));
        currencyList.setVisibleRowCount(10);
        logger.debug("listPanel created");
        return listPanel;
    }

    private JPanel createCurrencyPanel(String currency) {
        JPanel currencyPanel = new JPanel(new GridLayout(0, 1));

        String currencyInfo = currencyInfoMap.get(currency);
        if (currencyInfo != null) {
            String[] parts = currencyInfo.split(", ");
            JLabel nameLabel = new JLabel(" " + parts[1]);
            JLabel midRateLabel = new JLabel(" " + parts[2]);
            JLabel codeLabel = new JLabel(" " + parts[0]);
            
            Color newBackgroundColor = new Color(255, 223,  200); // Nowy kolor tła (czerwony)
            currencyPanel.setBackground(newBackgroundColor);

            currencyPanel.add(codeLabel);
            currencyPanel.add(nameLabel);
            currencyPanel.add(midRateLabel);

            int verticalGap = 10;
            currencyPanel.setBorder(BorderFactory.createEmptyBorder(verticalGap, 0, verticalGap, 0));

            Font labelFont = new Font("Arial", Font.BOLD, 14);
            nameLabel.setFont(labelFont);
            midRateLabel.setFont(labelFont);
            codeLabel.setFont(labelFont);

            if (parts.length >= 4) {
                JLabel bidLabel = new JLabel(" " + parts[3]);
                currencyPanel.add(bidLabel);
                bidLabel.setFont(labelFont);
            }

            if (parts.length >= 5) {
                JLabel askLabel = new JLabel(" " + parts[4]);
                currencyPanel.add(askLabel);
                askLabel.setFont(labelFont);
            }

            // Dodajemy obsługę kliknięcia w panel z informacjami o walucie
            currencyPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                	
                	if(isInternetAvailable(CHECK_URL)) {
                		
                        // jesli dostep do internetu, przelaczamy do panelu ze zdjeciem
                        cardLayout.show(cardPanel, "IMAGE_DISPLAY_" + currency);
                        logger.info("switching to imageCardPanel");
                	}else {
                		//jesli brak dostepu do internetu, przelaczamy do panelu z historia
                        cardLayout.show(cardPanel, "HISTORY_" + currency);
                        logger.info("Switching to historyPanel");
                		
                	}
                	
                }
            });
        }
        logger.debug("currencyPanel created");
        return currencyPanel;
    }
    
    // tworzenie panelu historii
    private JPanel createHistoryPanel(String currency) {
        logger.info("Tworzenie panelu historii dla waluty: " + currency);
        JPanel historyPanel = new JPanel(new BorderLayout());

        //tyutl dla panelu historia
        JLabel titleLabel = new JLabel("Historia kursu z ostatnich 6 dni");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        historyPanel.add(titleLabel, BorderLayout.NORTH);
        
        Color newBackgroundColor = new Color(224, 147, 180); // 
        historyPanel.setBackground(newBackgroundColor);

        JTextArea historyTextArea = new JTextArea();
        historyTextArea.setEditable(false); // nie mozna edytowac
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        Color newBackgroundColor2 = new Color(215, 177, 180); // 
        historyTextArea.setBackground(newBackgroundColor2);

        // Dodanie przycisku "Powrót"
        JButton backButton = new JButton("Powrót do informacji o walucie");
        backButton.addActionListener(e -> {
            // Przełącz do panelu informacji po kliknięciu w przycisk "Powrót"
            logger.info("switching to currencyCardPanel");
        	cardLayout.show(cardPanel, currency);
        });

        // Panel z przyciskiem
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        
        //dodanie przycisku powrotu do zdjecia
        JButton backToImageButton = new JButton("Powrót do zdjęcia");
        backToImageButton.addActionListener(e -> {
        	// Przełącz do panelu z obrazkiem po kliknięciu w przycisk "Powrót do zdjęcia"
            logger.info("switching to imagePanel");
            cardLayout.show(cardPanel, "IMAGE_DISPLAY_" + currency); // Załóżmy, że "IMAGE" to nazwa panelu z obrazkiem
        });
        
        if(isInternetAvailable(CHECK_URL)) {
        	buttonPanel.add(backToImageButton);
        }
        
        
        historyPanel.add(buttonPanel, BorderLayout.SOUTH);
        List<String> generatedDates = generateDatesLast7Days();

        // Wczytanie historii dla danej waluty
        try {
            List<String> lines = Files.readAllLines(Paths.get("kursy_waluty_last_7_days.txt"));
            StringBuilder historyText = new StringBuilder();
            boolean isCurrencyLine = false;
            int midRatesCount = 0;
            
            if(lines.isEmpty()) {
            	logger.warn("file kursy_waluty_last_7_days.txt is empty");
            }
            
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    if (key.equals("Currency") && value.equals(currency)) {
                        isCurrencyLine = true;
                    } else if (key.equals("Mid Rate") && isCurrencyLine) {
                    	for(int i = 0; i < generatedDates.size(); i++) {
                    	   }
                    		historyText.append(generatedDates.get(midRatesCount) + " --> " + line).append("\n");
                    		midRatesCount++;
                        if (midRatesCount >= 6) {
                            break;  // Zakończ wczytywanie po uzyskaniu 7 dni
                        }
                    }
                } else {
                    isCurrencyLine = false;
                }
            }
            //tworzenie czcionki
            Font customFont = new Font("Arial", Font.BOLD, 14); 
            historyTextArea.setFont(customFont);
            historyTextArea.setText(historyText.toString());
            historyPanel.setAlignmentX(historyTextArea.CENTER_ALIGNMENT);
            scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
            backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Dodaj komunikat na konsolę, który potwierdzi wczytanie historii dla danej waluty
            logger.debug("Wczytano historię dla waluty: " + currency);
            logger.info("Zawartość historii: " + historyText.toString());
        } catch (IOException e) {
            e.printStackTrace();
            //  komunikat na konsolę, jeśli wystąpił błąd podczas wczytywania historii
            logger.error("Błąd podczas wczytywania historii dla waluty: " + currency);
        }

        // Dodaj komunikat na konsolę, który potwierdzi utworzenie panelu historii
        logger.debug("Utworzono panel historii dla waluty: " + currency);

        return historyPanel;
    }

    //pobieranie inf o walucie 
    private Map<String, String> loadCurrencyInfo() {
        Map<String, String> infoMap = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("merged_currency_info.txt"));

            if(lines.isEmpty()) {
            	logger.warn("file merged_currency_info.txt is blank");
            }
            
            for (String line : lines) {
                String[] parts = line.split(", ");
                String currencyCode = parts[0].split(":")[1].trim();
                String currencyName = parts[1].split(":")[1].trim();
                String midRate = parts[2].split(":")[1].trim();
                String bid = "";
                String ask = "";

                // Sprawdzanie, czy dla danej waluty istnieją dane "bid" i "ask" w linii
                if (parts.length >= 4) {
                    bid = ", Bid: " + parts[3].split(":")[1].trim();
                }

                if (parts.length >= 5) {
                    ask = ", Ask: " + parts[4].split(":")[1].trim();
                }

                String info = String.format("Code: %s, Name: %s, Mid Rate: %s%s%s", currencyCode, currencyName, midRate, bid, ask);
                infoMap.put(currencyCode, info);
            }

        } catch (IOException e) {
            logger.error("Bład wczytywania informacji o walutach");
            e.printStackTrace();
        }
        logger.debug("infoMap created");
        return infoMap;
    }
    
    //generowanie daty
    public static List<String> generateDatesLast7Days() {
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i <= 6; i++) {
            LocalDate currentDate = today.minusDays(i);
            dates.add(currentDate.format(formatter));
        }
        //odwraca liste
        Collections.reverse(dates);

        return dates;
    }


    public static void main(String[] args) {
 
        SwingUtilities.invokeLater(() -> {
            List<String> currencies = List.of("THB", "USD", "AUD", "CAD", "SGD", "EUR", "HUF", "CHF", "GBP", "UAH",
                    "JPY", "CZK", "DKK", "ISK", "NOK", "SEK", "RON", "BGN", "TRY", "ILS",
                    "MXN", "IDR", "CNY");
            new CurrencyApp(currencies);
        });
    }
} 