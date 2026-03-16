package io.qoop.date.jalali;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateConversionDailyTest {

    @Test
    public void testDailyConversionFrom622To3000() {

        LocalDateTime startDate = LocalDateTime.of(622, 3, 22, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(20000, 1, 1, 0, 0, 0);
        LocalDateTime currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            JalaliDateTime parsiDate = JalaliConverter.toJalali(currentDate);
            LocalDateTime convertedGregorian = JalaliConverter.toGregorian(parsiDate);
            assertEquals(currentDate, convertedGregorian,
                    "Mismatch found at date: " + currentDate +
                            " -> Parsi: " + parsiDate.parsiStrDateTime());

            currentDate = currentDate.plusDays(1);

        }
    }
}