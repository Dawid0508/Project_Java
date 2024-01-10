package project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class zapas {
    public static void main(String[] args) {
        try {
            mergeCurrencyInfo("nazwy_walut.txt", "bid_ask.txt", "merged_currency_info.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mergeCurrencyInfo(String currencyInfoFile, String bidAskFile, String output) throws IOException {
        Map<String, String> currencyInfoMap = readCurrencyInfo(currencyInfoFile);
        Map<String, String> bidAskMap = readBidAskInfo(bidAskFile);

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(output), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (Map.Entry<String, String> entry : currencyInfoMap.entrySet()) {
                String currencyCode = entry.getKey();
                String currencyInfo = entry.getValue();

                if (bidAskMap.containsKey(currencyCode)) {
                    String bidAskInfo = bidAskMap.get(currencyCode);
                    writer.write(currencyInfo + ", " + bidAskInfo);
                } else {
                    writer.write(currencyInfo);
                }

                writer.newLine();
            }
        }
    }

    private static Map<String, String> readCurrencyInfo(String filePath) throws IOException {
        Map<String, String> currencyInfoMap = new HashMap<>();

        List<String> lines = Files.readAllLines(Path.of(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String currencyCode = parts[0].split(":")[1].trim();
                currencyInfoMap.put(currencyCode, line);
            }
        }

        return currencyInfoMap;
    }

    private static Map<String, String> readBidAskInfo(String filePath) throws IOException {
        Map<String, String> bidAskMap = new HashMap<>();

        List<String> lines = Files.readAllLines(Path.of(filePath));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String currencyCode = parts[0].split(":")[1].trim();
                String bidAskInfo = String.format("Bid: %s, Ask: %s", parts[1].split(":")[1].trim(), parts[2].split(":")[1].trim());
                bidAskMap.put(currencyCode, bidAskInfo);
            }
        }

        return bidAskMap;
    }
}
