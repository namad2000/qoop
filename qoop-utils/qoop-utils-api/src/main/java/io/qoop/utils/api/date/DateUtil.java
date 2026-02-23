package io.qoop.utils.api.date;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for converting between Persian (Jalali) and Gregorian calendars.
 */
public interface DateUtil {

    /**
     * Converts a Gregorian LocalDate to Persian date string in format yyyy/MM/dd.
     */
    String toParseDate(LocalDate localDate);

    /**
     * Returns the current Persian (Jalali) date in format yyyy/MM/dd.
     */
    String getCurrentPersianDate();

    /**
     * Converts a Gregorian LocalDateTime to Persian datetime string in format yyyyMMddHHmmss.
     */
    String toParseDateTime(LocalDateTime localDateTime);

    /**
     * Parses a Persian date string and converts it to Gregorian LocalDate.
     */
    LocalDate fromParsedDate(String formattedDate);

    /**
     * Parses a Persian datetime string in format yyyyMMddHHmmss and converts it to Gregorian LocalDateTime.
     */
    LocalDateTime fromParsedDateTime(String formattedDateTime);

    /**
     * Converts epoch milliseconds to Persian date string.
     */
    String fromParseDateMillisecond(Long millisecond);

    /**
     * Converts a compact Persian date string (yyyyMMdd) to Gregorian date string (yyyy/MM/dd).
     */
    String shamsiCompactToGregorian(String compact);

    /**
     * Parses a flexible date string supporting multiple formats and calendar systems.
     */
    LocalDate parseFlexibleDate(String input);

    /**
     * Converts a flexible date/time string to epoch milliseconds.
     */
    long epochMillisFromFlexible(String raw);

    /**
     * Normalizes input digits to English.
     */
    String normalizeDigits(String input);
}