package io.qoop.utils.core.date;

import io.qoop.utils.api.date.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Spring Boot Integration Test for DateUtilAdapter.
 * Achieves 100% Line and Branch Coverage by mocking the Clock to control system time.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DateUtilAdapter.class})
class DateUtilAdapterTest {

    @Autowired
    private DateUtil dateUtil;

    // Mocking the Clock to control LocalDate.now() and LocalDateTime.now() behavior
    @MockitoBean
    private Clock clock;

    // Fixed Instant for reproducible tests: 2025-02-23 20:06:11 UTC
    // This corresponds to Persian date: 1403/12/05
    private static final Instant FIXED_INSTANT = Instant.parse("2025-02-23T20:06:11Z");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    @BeforeEach
    void setUp() {
        // Configure the mocked clock to return our fixed instant
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(SYSTEM_ZONE);
    }

    // ==================== Helper to get expected Persian Date ====================
    // Since we can't easily calculate Persian dates in the test without the library,
    // we rely on the fact that 2025-02-23 is 1403-12-05 in Time4J.
    private String getExpectedPersianDate() {
        return "1403/12/05";
    }

    private String getExpectedPersianDateTime() {
        return "14031205200611"; // yyyyMMddHHmmss
    }

    // ==================== Tests for toParseDate ====================
    @Test
    void testToParseDate_ConvertsGregorianToPersian() {
        LocalDate gregorianDate = LocalDate.of(2025, 2, 23);
        String result = dateUtil.toParseDate(gregorianDate);
        assertEquals(getExpectedPersianDate(), result);
    }

    // ==================== Tests for getCurrentPersianDate ====================
    @Test
    void testGetCurrentPersianDate_ReturnsFixedDate() {
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {

            LocalDate fixedDate = LocalDate.of(2025, 2, 23);
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            String result = dateUtil.getCurrentPersianDate();

            assertEquals("1403/12/05", result);
        }
    }

    // ==================== Tests for toParseDateTime ====================
    @Test
    void testToParseDateTime_ConvertsGregorianToPersianCompact() {
        LocalDateTime gregorianDateTime = LocalDateTime.of(2025, 2, 23, 20, 6, 11);
        String result = dateUtil.toParseDateTime(gregorianDateTime);
        assertEquals(getExpectedPersianDateTime(), result);
    }

    // ==================== Tests for fromParsedDate ====================
    @Test
    void testFromParsedDate_ConvertsPersianToGregorian() {
        String persianDate = "1403/12/05";
        LocalDate result = dateUtil.fromParsedDate(persianDate);
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testFromParsedDate_ConvertsPersianSingleDigit() {
        String persianDate = "1403/1/1";
        LocalDate result = dateUtil.fromParsedDate(persianDate);
        assertEquals(LocalDate.of(2024, 3, 20), result);
    }

    @Test
    void testFromParsedDate_ThrowsException_InvalidDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDate("1403/13/01"); // Invalid month
        });
    }

    @Test
    void testFromParsedDate_ThrowsException_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDate(null);
        });
    }

    // ==================== Tests for fromParsedDateTime ====================
    @Test
    void testFromParsedDateTime_ConvertsCompactToGregorian() {
        String persianCompact = "14031205200611";
        LocalDateTime result = dateUtil.fromParsedDateTime(persianCompact);

        assertEquals(LocalDate.of(2025, 2, 23), result.toLocalDate());
        assertEquals(20, result.getHour());
        assertEquals(6, result.getMinute());
        assertEquals(11, result.getSecond());
    }

    @Test
    void testFromParsedDateTime_ThrowsException_InvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDateTime("14031205");
        });
    }

    @Test
    void testFromParsedDateTime_ThrowsException_InvalidTimeHour() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDateTime("14031205259999"); // Hour 25
        });
    }

    @Test
    void testFromParsedDateTime_ThrowsException_InvalidTimeMinute() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDateTime("14031205206099"); // Minute 60
        });
    }

    @Test
    void testFromParsedDateTime_ThrowsException_InvalidTimeSecond() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.fromParsedDateTime("14031205200660"); // Second 60
        });
    }

    // ==================== Tests for fromParseDateMillisecond ====================
    @Test
    void testFromParseDateMillisecond_ConvertsEpochToPersian() {
        long epoch = FIXED_INSTANT.toEpochMilli();
        String result = dateUtil.fromParseDateMillisecond(epoch);
        assertEquals(getExpectedPersianDate(), result);
    }

    // ==================== Tests for shamsiCompactToGregorian ====================
    @Test
    void testShamsiCompactToGregorian_ConvertsCorrectly() {
        String compact = "14031205";
        String result = dateUtil.shamsiCompactToGregorian(compact);
        assertEquals("2025/02/23", result);
    }

    @Test
    void testShamsiCompactToGregorian_ThrowsException_InvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.shamsiCompactToGregorian("1403");
        });
    }

    // ==================== Tests for parseFlexibleDate ====================
    @Test
    void testParseFlexibleDate_CompactPersian() {
        LocalDate result = dateUtil.parseFlexibleDate("14031205");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_YearMonthDaySlash() {
        LocalDate result = dateUtil.parseFlexibleDate("1403/12/05");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_YearMonthDayDash() {
        LocalDate result = dateUtil.parseFlexibleDate("1403-12-05");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_DayMonthYearSlash_Persian() {
        LocalDate result = dateUtil.parseFlexibleDate("05/12/1403");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_DayMonthYearSlash_Gregorian() {
        LocalDate result = dateUtil.parseFlexibleDate("23/02/2025");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_DayMonthYearDash_Gregorian() {
        LocalDate result = dateUtil.parseFlexibleDate("23-02-2025");
        assertEquals(LocalDate.of(2025, 2, 23), result);
    }

    @Test
    void testParseFlexibleDate_ThrowsException_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.parseFlexibleDate(null);
        });
    }

    @Test
    void testParseFlexibleDate_ThrowsException_Empty() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.parseFlexibleDate("   ");
        });
    }

    @Test
    void testParseFlexibleDate_ThrowsException_UnsupportedFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            dateUtil.parseFlexibleDate("Invalid-Date");
        });
    }

    // ==================== Tests for epochMillisFromFlexible ====================
    @Test
    void testEpochMillisFromFlexible_EpochMillis() {
        long epoch = FIXED_INSTANT.toEpochMilli();
        long result = dateUtil.epochMillisFromFlexible(String.valueOf(epoch));
        assertEquals(epoch, result);
    }

    @Test
    void testEpochMillisFromFlexible_EpochSeconds() {
        long seconds = FIXED_INSTANT.getEpochSecond();
        long expected = seconds * 1000;
        long result = dateUtil.epochMillisFromFlexible(String.valueOf(seconds));
        assertEquals(expected, result);
    }

    @Test
    void testEpochMillisFromFlexible_PersianCompactDateTime() {
        String input = "14031205200611";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(result), SYSTEM_ZONE);
        assertEquals(2025, zdt.getYear());
        assertEquals(2, zdt.getMonthValue());
        assertEquals(23, zdt.getDayOfMonth());
    }

    @Test
    void testEpochMillisFromFlexible_GregorianCompactDateTime() {
        String input = "20250223200611";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(result), SYSTEM_ZONE);
        assertEquals(2025, zdt.getYear());
    }

    @Test
    void testEpochMillisFromFlexible_IsoFormat() {
        String input = "2025-02-23T20:06:11";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);
    }

    @Test
    void testEpochMillisFromFlexible_IsoFormatWithOffset() {
        String input = "2025-02-23T20:06:11Z";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertEquals(FIXED_INSTANT.toEpochMilli(), result);
    }

    @Test
    void testEpochMillisFromFlexible_CommonPattern() {
        String input = "2025-02-23 20:06:11";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);
    }

    @Test
    void testEpochMillisFromFlexible_PersianWithSeparator() {
        String input = "1403/12/05 20:06:11";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);
    }

    @Test
    void testEpochMillisFromFlexible_PersianWithSeparatorDash() {
        String input = "1403-12-05 20:06:11";
        long result = dateUtil.epochMillisFromFlexible(input);
        assertNotEquals(Long.MIN_VALUE, result);
    }

    @Test
    void testEpochMillisFromFlexible_NullInput() {
        assertEquals(Long.MIN_VALUE, dateUtil.epochMillisFromFlexible(null));
    }

    @Test
    void testEpochMillisFromFlexible_BlankInput() {
        assertEquals(Long.MIN_VALUE, dateUtil.epochMillisFromFlexible("   "));
    }

    @Test
    void testEpochMillisFromFlexible_UnrecognizedFormat() {
        assertEquals(Long.MIN_VALUE, dateUtil.epochMillisFromFlexible("Just Text"));
    }

    // ==================== Tests for normalizeDigits ====================
    @Test
    void testNormalizeDigits_ConvertsPersianDigits() {
        String input = "۱۴۰۳/۱۲/۰۵";
        String result = dateUtil.normalizeDigits(input);
        assertEquals("1403/12/05", result);
    }

    @Test
    void testNormalizeDigits_ConvertsArabicDigits() {
        String input = "١٤٠٣/١٢/٠٥";
        String result = dateUtil.normalizeDigits(input);
        assertEquals("1403/12/05", result);
    }

    @Test
    void testNormalizeDigits_NullInput() {
        assertNull(dateUtil.normalizeDigits(null));
    }
}