package io.qoop.utils.core.date;

import io.qoop.utils.api.date.DateUtil;
import io.qoop.utils.api.strings.StringUtil;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

import static io.qoop.utils.api.strings.StringUtil.toEnglishNumbers;


/**
 * Utility class for converting between Persian (Jalali/Shamsi) and Gregorian calendars.
 * Uses Time4J library for accurate calendar conversions and leap year handling.
 * Implements {@link io.qoop.utils.api.date.DateUtil}.
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Component
public class DateUtilAdapter implements DateUtil {

    // Constants
    private static final int PERSIAN_YEAR_THRESHOLD = 1700;
    private static final int COMPACT_DATE_LENGTH = 8;
    private static final int COMPACT_DATETIME_LENGTH = 14;
    private static final int EPOCH_MILLIS_LENGTH = 13;
    private static final int EPOCH_SECONDS_LENGTH = 10;
    private static final int MIN_HOUR = 0;
    private static final int MAX_HOUR = 23;
    private static final int MIN_MINUTE = 0;
    private static final int MAX_MINUTE = 59;
    private static final int MIN_SECOND = 0;
    private static final int MAX_SECOND = 59;
    private static final String DATE_SEPARATOR = "/";
    private static final String DATE_SEPARATOR_REGEX = "[/-]";
    private static final String COMPACT_DATE_REGEX = "\\d{8}";
    private static final String COMPACT_DATETIME_REGEX = "\\d{14}";
    private static final String YEAR_MONTH_DAY_SLASH_REGEX = "\\d{4}/\\d{1,2}/\\d{1,2}";
    private static final String YEAR_MONTH_DAY_DASH_REGEX = "\\d{4}-\\d{1,2}-\\d{1,2}";
    private static final String DAY_MONTH_YEAR_SLASH_REGEX = "\\d{1,2}/\\d{1,2}/\\d{4}";
    private static final String DAY_MONTH_YEAR_DASH_REGEX = "\\d{1,2}-\\d{1,2}-\\d{4}";
    private static final String PERSIAN_DATE_PATTERN = "yyyy/M/d";
    private static final String COMPACT_DATE_PATTERN = "yyyyMMdd";
    private static final String GREGORIAN_COMPACT_DATETIME_PATTERN = "yyyyMMddHHmmss";
    private static final String GREGORIAN_DATE_FORMAT = "%04d/%02d/%02d";

    // Time4J formatters
    private static final ChronoFormatter<PersianCalendar> PERSIAN_DATE_FORMATTER =
            ChronoFormatter.ofPattern(PERSIAN_DATE_PATTERN, PatternType.CLDR, Locale.ROOT, PersianCalendar.axis());
    private static final ChronoFormatter<PersianCalendar> PERSIAN_DATE_PARSER =
            ChronoFormatter.ofPattern(PERSIAN_DATE_PATTERN, PatternType.CLDR, Locale.ROOT, PersianCalendar.axis());
    private static final Locale ENGLISH_LOCALE = Locale.ENGLISH;

    /**
     * Converts a Gregorian LocalDate to Persian date string in format yyyy/MM/dd.
     *
     * @param localDate Gregorian date to convert
     * @return Persian date string in format yyyy/MM/dd with zero-padding
     */
    @Override
    public String toParseDate(LocalDate localDate) {
        PersianCalendar persianDate = convertToPersianCalendar(localDate);
        String formatted = PERSIAN_DATE_FORMATTER.format(persianDate);
        return padDateComponents(formatted);
    }

    /**
     * Returns the current Persian (Jalali) date in format yyyy/MM/dd.
     * Uses the system's current date and converts it to Persian calendar.
     *
     * @return Current Persian date string in format yyyy/MM/dd with zero-padding
     */
    @Override
    public String getCurrentPersianDate() {
        return toParseDate(LocalDate.now());
    }

    /**
     * Converts a Gregorian LocalDateTime to Persian datetime string in format yyyyMMddHHmmss.
     *
     * @param localDateTime Gregorian datetime to convert
     * @return Persian datetime string in compact format (14 digits)
     */
    @Override
    public String toParseDateTime(LocalDateTime localDateTime) {
        PersianCalendar persianDate = convertToPersianCalendar(localDateTime.toLocalDate());
        String datePart = formatPersianDateCompact(persianDate);
        String timePart = formatTimeCompact(localDateTime);
        return datePart + timePart;
    }

    /**
     * Parses a Persian date string and converts it to Gregorian LocalDate.
     * Supports formats like "1403/01/01", "1403/1/1", etc.
     *
     * @param formattedDate Persian date string
     * @return Gregorian LocalDate
     * @throws IllegalArgumentException if the date is invalid or cannot be parsed
     */
    @Override
    public LocalDate fromParsedDate(String formattedDate) {
        try {
            String normalized = normalizePersianDateInput(formattedDate);
            PersianCalendar persianDate = PERSIAN_DATE_PARSER.parse(normalized);
            return convertToLocalDate(persianDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse date: " + formattedDate, e);
        }
    }

    /**
     * Parses a Persian datetime string in format yyyyMMddHHmmss and converts it to Gregorian LocalDateTime.
     *
     * @param formattedDateTime Persian datetime string (14 digits)
     * @return Gregorian LocalDateTime
     * @throws IllegalArgumentException if the datetime is invalid or cannot be parsed
     */
    @Override
    public LocalDateTime fromParsedDateTime(String formattedDateTime) {
        String normalized = extractDigitsOnly(formattedDateTime);
        validateDateTimeLength(normalized);
        String datePart = extractDatePart(normalized);
        String timePart = extractTimePart(normalized);
        LocalDate localDate = parsePersianDateFromCompact(datePart);
        java.time.LocalTime localTime = parseTimeComponents(timePart);
        return LocalDateTime.of(localDate, localTime);
    }

    /**
     * Converts epoch milliseconds to Persian date string.
     *
     * @param millisecond Epoch milliseconds
     * @return Persian date string in format yyyy/MM/dd
     */
    @Override
    public String fromParseDateMillisecond(Long millisecond) {
        LocalDate localDate = Instant.ofEpochMilli(millisecond)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return toParseDate(localDate);
    }

    /**
     * Converts a compact Persian date string (yyyyMMdd) to Gregorian date string (yyyy/MM/dd).
     *
     * @param compact Persian date in format yyyyMMdd (e.g., "13730914")
     * @return Gregorian date in format yyyy/MM/dd (e.g., "1980/09/19")
     * @throws IllegalArgumentException if the input is invalid
     */
    @Override
    public String shamsiCompactToGregorian(String compact) {
        String normalized = extractDigitsOnly(compact);
        validateCompactDateLength(normalized);
        String formattedDate = formatCompactDateForParsing(normalized);
        LocalDate gregorian = fromParsedDate(formattedDate);
        return formatGregorianDate(gregorian);
    }

    /**
     * Parses a flexible date string supporting multiple formats and calendar systems.
     * Supports:
     * - Persian or English digits
     * - Formats: yyyyMMdd, yyyy/MM/dd, yyyy-MM-dd, dd/MM/yyyy, ...
     * - Persian (Shamsi) or Gregorian calendar
     *
     * @param input Date string in various formats
     * @return LocalDate in Gregorian calendar
     * @throws IllegalArgumentException if the input is null, empty, or cannot be parsed
     */
    @Override
    public LocalDate parseFlexibleDate(String input) {
        validateInput(input);
        String normalized = normalizeInput(input);
        if (isCompactDate(normalized)) {
            return parseCompactDate(normalized);
        }
        if (isYearMonthDaySlashFormat(normalized)) {
            return parseByPattern(normalized, PERSIAN_DATE_PATTERN);
        }
        if (isYearMonthDayDashFormat(normalized)) {
            return parseByPattern(normalized, "yyyy-M-d");
        }
        if (isDayMonthYearSlashFormat(normalized)) {
            return parseDayMonthYearFormat(normalized, DATE_SEPARATOR);
        }
        if (isDayMonthYearDashFormat(normalized)) {
            return parseDayMonthYearFormat(normalized, "-");
        }
        throw new IllegalArgumentException("Unsupported date format: " + input);
    }

    /**
     * Converts a flexible date/time string to epoch milliseconds.
     * Supports multiple formats including epoch timestamps, ISO formats, and various date/time patterns.
     *
     * @param raw Date/time string in various formats
     * @return Epoch milliseconds, or Long.MIN_VALUE if unrecognized
     */
    @Override
    public long epochMillisFromFlexible(String raw) {
        if (raw == null || raw.isBlank()) {
            return Long.MIN_VALUE;
        }
        String normalized = toEnglishNumbers(raw.trim());
        Long result = tryParseEpochTimestamp(normalized);
        if (result != null) return result;
        result = tryParsePersianCompactDateTime(normalized);
        if (result != null) return result;
        result = tryParseGregorianCompactDateTime(normalized);
        if (result != null) return result;
        result = tryParseCompactDate(normalized);
        if (result != null) return result;
        result = tryParseIsoFormats(normalized);
        if (result != null) return result;
        result = tryParseCommonDateTimePatterns(normalized);
        if (result != null) return result;
        result = tryParsePersianWithSeparator(normalized);
        if (result != null) return result;
        return Long.MIN_VALUE;
    }

    /**
     * Normalizes input digits to English.
     *
     * @param input String with potentially Persian digits
     * @return String with English digits, or null if input is null
     */
    @Override
    public String normalizeDigits(String input) {
        if (input == null) {
            return null;
        }
        return toEnglishNumbers(input);
    }

    // ==================== Private Helper Methods ====================

    private PersianCalendar convertToPersianCalendar(LocalDate localDate) {
        PlainDate gregorianDate = PlainDate.from(localDate);
        return gregorianDate.transform(PersianCalendar.class);
    }

    private LocalDate convertToLocalDate(PersianCalendar persianDate) {
        PlainDate gregorianDate = persianDate.transform(PlainDate.class);
        return gregorianDate.toTemporalAccessor();
    }

    private String padDateComponents(String date) {
        String[] parts = toEnglishNumbers(date).split(DATE_SEPARATOR);
        return Arrays.stream(parts)
                .map(part -> part.length() <= 1 ? "0" + part : part)
                .reduce((a, b) -> a + DATE_SEPARATOR + b)
                .orElse(date);
    }

    private String normalizePersianDateInput(String formattedDate) {
        String withEnglishDigits = toEnglishNumbers(formattedDate);
        return removeLeadingZeros(fromDateComponents(withEnglishDigits));
    }

    private String removeLeadingZeros(String date) {
        String[] parts = date.split(DATE_SEPARATOR);
        return Arrays.stream(parts)
                .map(part -> part.replaceFirst("^0+(?!$)", ""))
                .reduce((a, b) -> a + DATE_SEPARATOR + b)
                .orElse(date);
    }

    private String fromDateComponents(String date) {
        return date;
    }

    private String formatPersianDateCompact(PersianCalendar persianDate) {
        int year = persianDate.getYear();
        int month = persianDate.getMonth().getValue();
        int day = persianDate.getDayOfMonth();
        return String.format("%04d%02d%02d", year, month, day);
    }

    private String formatTimeCompact(LocalDateTime localDateTime) {
        int hour = localDateTime.getHour();
        int minute = localDateTime.getMinute();
        int second = localDateTime.getSecond();
        return String.format("%02d%02d%02d", hour, minute, second);
    }

    private String extractDigitsOnly(String input) {
        return toEnglishNumbers(input).replaceAll("\\D", "");
    }

    private void validateDateTimeLength(String normalized) {
        if (normalized.length() != COMPACT_DATETIME_LENGTH) {
            throw new IllegalArgumentException(
                    "Invalid input: expected " + COMPACT_DATETIME_LENGTH + " digits (yyyyMMddHHmmss)");
        }
    }

    private String extractDatePart(String normalized) {
        return normalized.substring(0, COMPACT_DATE_LENGTH);
    }

    private String extractTimePart(String normalized) {
        return normalized.substring(COMPACT_DATE_LENGTH, COMPACT_DATETIME_LENGTH);
    }

    private LocalDate parsePersianDateFromCompact(String datePart) {
        String formattedDate = formatCompactDateForParsing(datePart);
        return fromParsedDate(formattedDate);
    }

    private String formatCompactDateForParsing(String compactDate) {
        int year = Integer.parseInt(compactDate.substring(0, 4));
        int month = Integer.parseInt(compactDate.substring(4, 6));
        int day = Integer.parseInt(compactDate.substring(6, 8));
        return String.format("%d/%d/%d", year, month, day);
    }

    private java.time.LocalTime parseTimeComponents(String timePart) {
        int hour = Integer.parseInt(timePart.substring(0, 2));
        int minute = Integer.parseInt(timePart.substring(2, 4));
        int second = Integer.parseInt(timePart.substring(4, 6));
        validateTimeComponent(hour, MIN_HOUR, MAX_HOUR, "hour");
        validateTimeComponent(minute, MIN_MINUTE, MAX_MINUTE, "minute");
        validateTimeComponent(second, MIN_SECOND, MAX_SECOND, "second");
        return java.time.LocalTime.of(hour, minute, second);
    }

    private void validateTimeComponent(int value, int min, int max, String componentName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("Invalid %s: %d (must be %d-%d)", componentName, value, min, max));
        }
    }

    private void validateCompactDateLength(String normalized) {
        if (normalized.length() != COMPACT_DATE_LENGTH) {
            throw new IllegalArgumentException(
                    "Invalid input: expected " + COMPACT_DATE_LENGTH + " digits (yyyyMMdd)");
        }
    }

    private String formatGregorianDate(LocalDate gregorian) {
        return String.format(GREGORIAN_DATE_FORMAT,
                gregorian.getYear(),
                gregorian.getMonthValue(),
                gregorian.getDayOfMonth());
    }

    private void validateInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
    }

    private String normalizeInput(String input) {
        return StringUtil.toEnglishNumbers(input.trim())
                .replaceAll("[٫٬,]", "")
                .replaceAll("\\s+", "");
    }

    private boolean isCompactDate(String normalized) {
        return normalized.matches(COMPACT_DATE_REGEX);
    }

    private boolean isYearMonthDaySlashFormat(String normalized) {
        return normalized.matches(YEAR_MONTH_DAY_SLASH_REGEX);
    }

    private boolean isYearMonthDayDashFormat(String normalized) {
        return normalized.matches(YEAR_MONTH_DAY_DASH_REGEX);
    }

    private boolean isDayMonthYearSlashFormat(String normalized) {
        return normalized.matches(DAY_MONTH_YEAR_SLASH_REGEX);
    }

    private boolean isDayMonthYearDashFormat(String normalized) {
        return normalized.matches(DAY_MONTH_YEAR_DASH_REGEX);
    }

    private LocalDate parseDayMonthYearFormat(String normalized, String separator) {
        int year = extractYearFromEnd(normalized);
        if (isGregorianYear(year)) {
            return parseGregorianDayMonthYear(normalized, separator);
        }
        return parsePersianDayMonthYear(normalized, separator);
    }

    private LocalDate parsePersianDayMonthYear(String normalized, String separator) {
        String[] parts = normalized.split(separator);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format: " + normalized);
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        PersianCalendar persianDate = PersianCalendar.of(year, month, day);
        return convertToLocalDate(persianDate);
    }

    private int extractYearFromEnd(String normalized) {
        return Integer.parseInt(normalized.substring(normalized.length() - 4));
    }

    private boolean isGregorianYear(int year) {
        return year >= PERSIAN_YEAR_THRESHOLD;
    }

    private LocalDate parseGregorianDayMonthYear(String normalized, String separator) {
        String[] parts = normalized.split(separator);
        if (parts.length == 3) {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        }
        throw new IllegalArgumentException("Invalid date format: " + normalized);
    }

    private LocalDate parseCompactDate(String normalized) {
        return parseByPattern(normalized, COMPACT_DATE_PATTERN);
    }

    private LocalDate parseByPattern(String date, String pattern) {
        int year = extractYearFromPattern(date, pattern);
        boolean isPersian = !isGregorianYear(year);
        if (isPersian) {
            return parsePersianDate(date, pattern);
        } else {
            return parseGregorianDate(date, pattern);
        }
    }

    private int extractYearFromPattern(String date, String pattern) {
        if (pattern.startsWith("yyyy")) {
            return Integer.parseInt(date.substring(0, 4));
        } else if (pattern.endsWith("yyyy")) {
            return Integer.parseInt(date.substring(date.length() - 4));
        }
        throw new IllegalArgumentException("Cannot determine year position in pattern: " + pattern);
    }

    private LocalDate parsePersianDate(String date, String pattern) {
        String[] dateComponents = extractPersianDateComponents(date);
        int year = Integer.parseInt(dateComponents[0]);
        int month = Integer.parseInt(dateComponents[1]);
        int day = Integer.parseInt(dateComponents[2]);
        PersianCalendar persianDate = PersianCalendar.of(year, month, day);
        return convertToLocalDate(persianDate);
    }

    private String[] extractPersianDateComponents(String date) {
        String normalized = date.replaceAll(DATE_SEPARATOR_REGEX, DATE_SEPARATOR);
        String[] parts = normalized.split(DATE_SEPARATOR);
        if (parts.length == 3) {
            return parts;
        }
        if (date.length() == COMPACT_DATE_LENGTH) {
            return new String[]{
                    date.substring(0, 4),
                    date.substring(4, 6),
                    date.substring(6, 8)
            };
        }
        throw new IllegalArgumentException("Invalid Persian date format: " + date);
    }

    private LocalDate parseGregorianDate(String date, String pattern) {
        if (isCompactPattern(pattern)) {
            return parseGregorianCompactDate(date);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, ENGLISH_LOCALE);
        try {
            LocalDate parsed = LocalDate.parse(date, formatter);
            validateParsedDateMatchesInput(date, parsed, pattern);
            return parsed;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse date: " + date, e);
        } catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException("Invalid date: " + date, e);
        }
    }

    private boolean isCompactPattern(String pattern) {
        return COMPACT_DATE_PATTERN.equals(pattern);
    }

    private LocalDate parseGregorianCompactDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(COMPACT_DATE_PATTERN, ENGLISH_LOCALE);
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse date: " + date, e);
        }
    }

    private void validateParsedDateMatchesInput(String input, LocalDate parsed, String pattern) {
        if (!shouldValidateDateComponents(pattern)) {
            return;
        }
        DateComponents inputComponents = extractDateComponentsFromInput(input, pattern);
        if (!matchesParsedDate(inputComponents, parsed)) {
            throw new IllegalArgumentException("Invalid date: " + input);
        }
    }

    private boolean shouldValidateDateComponents(String pattern) {
        return pattern.contains("M") && pattern.contains("d");
    }

    private DateComponents extractDateComponentsFromInput(String input, String pattern) {
        String[] parts = input.replaceAll(DATE_SEPARATOR_REGEX, DATE_SEPARATOR).split(DATE_SEPARATOR);
        if (parts.length != 3) {
            return null;
        }
        if (pattern.startsWith("yyyy")) {
            return new DateComponents(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        } else {
            return new DateComponents(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[0])
            );
        }
    }

    private boolean matchesParsedDate(DateComponents input, LocalDate parsed) {
        if (input == null) {
            return true;
        }
        return parsed.getYear() == input.year &&
                parsed.getMonthValue() == input.month &&
                parsed.getDayOfMonth() == input.day;
    }

    // Epoch conversion helpers
    private Long tryParseEpochTimestamp(String normalized) {
        if (normalized.matches("\\d{" + EPOCH_MILLIS_LENGTH + "}")) {
            return parseLongSafely(normalized);
        }
        if (normalized.matches("\\d{" + EPOCH_SECONDS_LENGTH + "}")) {
            Long seconds = parseLongSafely(normalized);
            return seconds != null ? seconds * 1000L : null;
        }
        return null;
    }

    private Long tryParsePersianCompactDateTime(String normalized) {
        if (normalized.matches(COMPACT_DATETIME_REGEX) && isPersianYearPrefix(normalized)) {
            return convertPersianDateTimeToEpoch(normalized);
        }
        return null;
    }

    private boolean isPersianYearPrefix(String normalized) {
        return normalized.startsWith("13") || normalized.startsWith("14");
    }

    private Long convertPersianDateTimeToEpoch(String normalized) {
        try {
            LocalDateTime dateTime = fromParsedDateTime(normalized);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParseGregorianCompactDateTime(String normalized) {
        if (normalized.matches(COMPACT_DATETIME_REGEX)) {
            return parseGregorianDateTimeCompact(normalized);
        }
        return null;
    }

    private Long parseGregorianDateTimeCompact(String normalized) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(normalized,
                    DateTimeFormatter.ofPattern(GREGORIAN_COMPACT_DATETIME_PATTERN));
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParseCompactDate(String normalized) {
        if (normalized.matches(COMPACT_DATE_REGEX)) {
            return parseCompactDateToEpoch(normalized);
        }
        return null;
    }

    private Long parseCompactDateToEpoch(String normalized) {
        try {
            LocalDate date;
            if (isPersianYearPrefix(normalized)) {
                date = parsePersianDateFromCompact(normalized);
            } else {
                date = LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParseIsoFormats(String normalized) {
        Long result = tryParseIsoWithOffset(normalized);
        if (result != null) return result;
        return tryParseIsoWithoutOffset(normalized);
    }

    private Long tryParseIsoWithOffset(String normalized) {
        try {
            return java.time.OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParseIsoWithoutOffset(String normalized) {
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParseCommonDateTimePatterns(String normalized) {
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/M/d H:m:s",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-M-d H:m:s"
        };
        for (String pattern : patterns) {
            Long result = tryParseWithPattern(normalized, pattern);
            if (result != null) return result;
        }
        return null;
    }

    private Long tryParseWithPattern(String normalized, String pattern) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern(pattern));
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long tryParsePersianWithSeparator(String normalized) {
        if (isPersianYearPrefix(normalized) && matchesPersianWithSeparatorPattern(normalized)) {
            return parsePersianWithSeparatorToEpoch(normalized);
        }
        return null;
    }

    private boolean matchesPersianWithSeparatorPattern(String normalized) {
        return normalized.matches("\\d{4}[/\\-]\\d{1,2}[/\\-]\\d{1,2}(\\s+\\d{1,2}:\\d{1,2}:\\d{1,2})?");
    }

    private Long parsePersianWithSeparatorToEpoch(String normalized) {
        try {
            String digits = normalized.replaceAll("\\D", "");
            String compact14 = digits.length() == COMPACT_DATETIME_LENGTH ? digits : (digits + "000000");
            LocalDateTime dateTime = fromParsedDateTime(compact14);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper class for date components
    private class DateComponents {
        final int year;
        final int month;
        final int day;

        DateComponents(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }
}