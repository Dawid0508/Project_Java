package project;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class testy {

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
        List<String> generatedDates = generateDatesLast7Days();
        for (String date : generatedDates) {
            System.out.println(date);
        }
    }
}
