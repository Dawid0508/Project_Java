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
        if (!isValueExists(value)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                String property = key + " = " + value;
                writer.write(property);
                writer.newLine();
                LOGGER.info("Property saved: " + property);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("An error occurred while saving property to file.", e);
            }
        } else {
            LOGGER.info("Property already exists: " + value);
        }
    }

    private boolean isValueExists(String value) {
        return properties.values().stream().anyMatch(existingValue -> existingValue.equals(value));
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void loadProperties() {
        try (FileInputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            // Jeśli plik nie istnieje, zostanie utworzony przy pierwszym zapisie
        }
    }
}
