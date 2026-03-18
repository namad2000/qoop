package io.qoop.date.jalali;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class JalaliDateTest {

    @Test
    void testConstants() {
        assertNotNull(JalaliDate.MIN);
        assertEquals(1, JalaliDate.MIN.year());
        assertEquals(1, JalaliDate.MIN.monthValue());
        assertEquals(1, JalaliDate.MIN.dayOfMonth());

        assertNotNull(JalaliDate.MAX);
        assertEquals(Long.MAX_VALUE, JalaliDate.MAX.year());
        assertEquals(12, JalaliDate.MAX.monthValue());
        assertEquals(29, JalaliDate.MAX.dayOfMonth());
    }

    @Test
    void testNow() {
        JalaliDate jDate = JalaliDate.now();
        assertNotNull(jDate);
        LocalDate today = LocalDate.now();
        JalaliDate expected = JalaliDate.from(today);
        assertEquals(expected.year(), jDate.year());
        assertEquals(expected.monthValue(), jDate.monthValue());
        assertEquals(expected.dayOfMonth(), jDate.dayOfMonth());
    }

    @Test
    void testNowWithZone() {
        JalaliDate jDate = JalaliDate.now(ZoneId.of("Asia/Tehran"));
        assertNotNull(jDate);
        assertEquals(JalaliDate.now().year(), jDate.year());
    }

    @Test
    void testOfValidDate() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertEquals(1403, date.year());
        assertEquals(1, date.monthValue());
        assertEquals(1, date.dayOfMonth());
    }

    @Test
    void testOfInvalidYear() {
        JalaliDateTimeException ex = assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(0, 1, 1));
        assertEquals("JALALI_DATE_03", ex.getCode());

        assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(-1, 1, 1));
    }

    @Test
    void testOfInvalidMonth() {
        JalaliDateTimeException ex = assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(1403, 0, 1));
        assertEquals("JALALI_DATE_02", ex.getCode());

        assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(1403, 13, 1));
    }

    @Test
    void testOfInvalidDay() {
        // Farvardin has 31 days
        JalaliDateTimeException ex = assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(1403, 1, 32));
        assertEquals("JALALI_DATE_06", ex.getCode());

        // Esfand usually has 29 days (1403 is leap, so 30 is valid, 31 is invalid)
        assertThrows(JalaliDateTimeException.class, () -> JalaliDate.of(1403, 12, 31));
    }

    @Test
    void testOfLeapYearDay() {
        // 1403 is a leap year in Jalali, so Esfand has 30 days.
        JalaliDate date = JalaliDate.of(1403, 12, 30);
        assertEquals(1403, date.year());
        assertEquals(12, date.monthValue());
        assertEquals(30, date.dayOfMonth());
    }

    @Test
    void testGetters() {
        JalaliDate date = JalaliDate.of(1403, 5, 15);
        assertEquals(1403, date.year());
        assertEquals(5, date.monthValue());
        assertEquals(15, date.dayOfMonth());
        assertEquals(JalaliMonth.MORDAD, date.month());
    }

    @Test
    void testDayOfWeek() {
        // 1403/01/01 is CHAHAR_SHANBEH
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertEquals(JalaliDayOfWeek.CHAHAR_SHANBEH, date.dayOfWeek());
    }

    @Test
    void testPlusYearsZero() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertSame(date, date.plusYears(0));
    }

    @Test
    void testPlusYearsPositive() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate nextYear = date.plusYears(1);
        assertEquals(1404, nextYear.year());
        assertEquals(1, nextYear.monthValue());
        assertEquals(1, nextYear.dayOfMonth());
    }

    @Test
    void testPlusYearsNegative() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate prevYear = date.plusYears(-1);
        assertEquals(1402, prevYear.year());
    }

    @Test
    void testMinusYearsPositive() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate prevYear = date.minusYears(1);
        assertEquals(1402, prevYear.year());
    }

    @Test
    void testMinusYearsNegative() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate nextYear = date.minusYears(-1);
        assertEquals(1404, nextYear.year());
    }

    @Test
    void testPlusMonthsZero() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertSame(date, date.plusMonths(0));
    }

    @Test
    void testPlusMonthsPositive_Simple() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate feb = date.plusMonths(1);
        assertEquals(1403, feb.year());
        assertEquals(2, feb.monthValue());
    }

    @Test
    void testPlusMonthsPositive_YearRollover() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate nextYear = date.plusMonths(12);
        assertEquals(1404, nextYear.year());
        assertEquals(1, nextYear.monthValue());
    }

    @Test
    void testPlusMonthsPositive_MultipleYears() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate result = date.plusMonths(24);
        assertEquals(1405, result.year());
        assertEquals(1, result.monthValue());
    }

    @Test
    void testPlusMonthsNegative_Simple() {
        JalaliDate date = JalaliDate.of(1403, 5, 1);
        JalaliDate april = date.plusMonths(-1);
        assertEquals(1403, april.year());
        assertEquals(4, april.monthValue());
    }

    @Test
    void testPlusMonthsNegative_YearRollover() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate prevYearDec = date.plusMonths(-1);
        assertEquals(1402, prevYearDec.year());
        assertEquals(12, prevYearDec.monthValue());
    }

    @Test
    void testMinusMonthsPositive() {
        JalaliDate date = JalaliDate.of(1403, 5, 1);
        JalaliDate april = date.minusMonths(1);
        assertEquals(1403, april.year());
        assertEquals(4, april.monthValue());
    }

    @Test
    void testMinusMonthsNegative() {
        JalaliDate date = JalaliDate.of(1403, 5, 1);
        JalaliDate june = date.minusMonths(-1);
        assertEquals(1403, june.year());
        assertEquals(6, june.monthValue());
    }

    @Test
    void testPlusDaysZero() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertSame(date, date.plusDays(0));
    }

    @Test
    void testPlusDaysPositive() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate nextDay = date.plusDays(1);
        assertEquals(1403, nextDay.year());
        assertEquals(1, nextDay.monthValue());
        assertEquals(2, nextDay.dayOfMonth());
    }

    @Test
    void testPlusDaysNegative() {
        JalaliDate date = JalaliDate.of(1403, 1, 2);
        JalaliDate prevDay = date.plusDays(-1);
        assertEquals(1403, prevDay.year());
        assertEquals(1, prevDay.monthValue());
        assertEquals(1, prevDay.dayOfMonth());
    }

    @Test
    void testMinusDaysPositive() {
        JalaliDate date = JalaliDate.of(1403, 1, 2);
        JalaliDate prevDay = date.minusDays(1);
        assertEquals(1403, prevDay.year());
        assertEquals(1, prevDay.monthValue());
        assertEquals(1, prevDay.dayOfMonth());
    }

    @Test
    void testMinusDaysNegative() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        JalaliDate nextDay = date.minusDays(-1);
        assertEquals(1403, nextDay.year());
        assertEquals(1, nextDay.monthValue());
        assertEquals(2, nextDay.dayOfMonth());
    }

    @Test
    void testPlusDays_LeapYearBoundary() {
        // 1403 is leap. 1403/12/30 exists. +1 day should be 1404/01/01
        JalaliDate date = JalaliDate.of(1403, 12, 30);
        JalaliDate nextDay = date.plusDays(1);
        assertEquals(1404, nextDay.year());
        assertEquals(1, nextDay.monthValue());
        assertEquals(1, nextDay.dayOfMonth());
    }

    @Test
    void testFromLocalDate() {
        LocalDate gregorian = LocalDate.of(2024, 3, 20); // 1403/01/01
        JalaliDate jDate = JalaliDate.from(gregorian);
        assertEquals(1403, jDate.year());
        assertEquals(1, jDate.monthValue());
        assertEquals(1, jDate.dayOfMonth());
    }

    @Test
    void testToLocalDate() {
        JalaliDate jDate = JalaliDate.of(1403, 1, 1);
        LocalDate gregorian = jDate.toLocalDate();
        assertEquals(2024, gregorian.getYear());
        assertEquals(3, gregorian.getMonthValue());
        assertEquals(20, gregorian.getDayOfMonth());
    }

    @Test
    void testToString_SingleDigitMonthDay() {
        JalaliDate date = JalaliDate.of(1403, 1, 1);
        assertEquals("1403/01/01", date.toString());
    }

    @Test
    void testToString_DoubleDigitMonthDay() {
        JalaliDate date = JalaliDate.of(1403, 12, 29);
        assertEquals("1403/12/29", date.toString());
    }
}