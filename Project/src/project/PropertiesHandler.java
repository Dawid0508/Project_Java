package project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesHandler {
    private String fileName;
    private Properties properties;
    private static final Logger LOGGER = LogManager.getLogger(PropertiesHandler.class);

    public PropertiesHandler(String fileName) {
        this.fileName = fileName;
        this.properties = new Properties();
        loadProperties();
    }

    public void saveProperty(Object key, String value) {
        if (!isKeyExists(key)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                properties.setProperty(key.toString(), value);
                LOGGER.debug("klucz" + key + "wartosc" + value);
                writer.write(key + " = " + value);
                writer.newLine();
                LOGGER.info("Property saved: " + key + " = " + value);
 
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("An error occurred while saving property to file.", e);
            }
        } else {
            LOGGER.info("Property already exists for key: " + key);
        }

    }
    
    public void saveProperty(String value) {
        if (!isValueExists(value)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(value);
                writer.newLine();
                LOGGER.debug("Property saved: " + value);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("An error occurred while saving property to file.", e);
            }
        } else {
            LOGGER.info("Property already exists: " + value);
        }
    }

    private boolean isValueExists(String value) {
        return properties.containsValue(value.toString());
    }
    
    public boolean doesLineExist(String line) {
        try (FileInputStream input = new FileInputStream(fileName)) {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            String fileContent = new String(buffer);

            return fileContent.contains(line);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("An error occurred while checking if line exists in the file.", e);
            return false;
        }
    }

    private boolean isKeyExists(Object key) {
        return properties.containsKey(key.toString());
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void loadProperties() {
        try (FileInputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            // Jeśli plik nie istnieje, zostanie utworzony przy pierwszym zapisie
        	 e.printStackTrace();
             LOGGER.error("An error occurred while loading properties from file.", e);
        }
    }
}
