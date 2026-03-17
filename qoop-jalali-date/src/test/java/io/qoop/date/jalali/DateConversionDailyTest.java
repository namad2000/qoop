package io.qoop.date.jalali;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateConversionDailyTest {

    @Test
    public void testDailyConversionFrom622To3000() {

        LocalDate startDate = LocalDate.of(622, 3, 22);
        LocalDate endDate = LocalDate.of(20000, 1, 1);
        LocalDate currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            JalaliDate jalaliDate = JalaliConverter.toJalali(currentDate);
            LocalDate convertedGregorian = JalaliConverter.toGregorian(jalaliDate);
            assertEquals(currentDate, convertedGregorian,
                    "Mismatch found at date: " + currentDate +
                            " -> Parsi: " + jalaliDate);

            currentDate = currentDate.plusDays(1);

        }
    }
}