package io.qoop.utils.api.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    // ==================== Tests for isEmpty ====================
    @Test
    void testIsEmpty_ShouldReturnTrue_WhenStringIsNull() {
        assertTrue(StringUtil.isEmpty(null));
    }

    @Test
    void testIsEmpty_ShouldReturnTrue_WhenStringIsEmpty() {
        assertTrue(StringUtil.isEmpty(""));
    }

    @Test
    void testIsEmpty_ShouldReturnFalse_WhenStringHasContent() {
        assertFalse(StringUtil.isEmpty("Hello"));
        assertFalse(StringUtil.isEmpty("   "));
    }

    // ==================== Tests for isBlank ====================
    @Test
    void testIsBlank_ShouldReturnTrue_WhenStringIsNull() {
        assertTrue(StringUtil.isBlank(null));
    }

    @Test
    void testIsBlank_ShouldReturnTrue_WhenStringIsEmpty() {
        assertTrue(StringUtil.isBlank(""));
    }

    @Test
    void testIsBlank_ShouldReturnTrue_WhenStringContainsOnlyWhitespace() {
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\n"));
    }

    @Test
    void testIsBlank_ShouldReturnFalse_WhenStringHasText() {
        assertFalse(StringUtil.isBlank("Hello"));
        assertFalse(StringUtil.isBlank(" Hello "));
    }

    // ==================== Tests for toEnglishNumbers ====================
    @Test
    void testToEnglishNumbers_ShouldReturnNull_WhenInputIsNull() {
        assertNull(StringUtil.toEnglishNumbers(null));
    }

    @Test
    void testToEnglishNumbers_ShouldReturnEmpty_WhenInputIsEmpty() {
        assertEquals("", StringUtil.toEnglishNumbers(""));
    }

    @Test
    void testToEnglishNumbers_ShouldConvertPersianDigits() {
        // ۰۱۲۳۴۵۶۷۸۹
        String persian = "\u06F0\u06F1\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8\u06F9";
        String expected = "0123456789";
        assertEquals(expected, StringUtil.toEnglishNumbers(persian));
    }

    @Test
    void testToEnglishNumbers_ShouldConvertArabicDigits() {
        // ٠١٢٣٤٥٦٧٨٩
        String arabic = "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669";
        String expected = "0123456789";
        assertEquals(expected, StringUtil.toEnglishNumbers(arabic));
    }

    @Test
    void testToEnglishNumbers_ShouldConvertMixedDigits() {
        // Input: ۱۲۳۴۵۶۷۸۹۰ (Persian) mixed with text
        String input = "کد: ۱۲۳۴۵۶۷۸۹۰";
        String expected = "کد: 1234567890";
        assertEquals(expected, StringUtil.toEnglishNumbers(input));
    }

    @Test
    void testToEnglishNumbers_ShouldKeepEnglishDigitsUnchanged() {
        String input = "1234567890";
        assertEquals(input, StringUtil.toEnglishNumbers(input));
    }

    @Test
    void testToEnglishNumbers_ShouldHandleMixedPersianAndArabicDigits() {
        // Example: ۱۲۳ (Persian) + ٤٥٦ (Arabic)
        String input = "\u06F1\u06F2\u06F3\u0664\u0665\u0666";
        String expected = "123456";
        assertEquals(expected, StringUtil.toEnglishNumbers(input));
    }

    @Test
    void testToEnglishNumbers_ShouldIgnoreNonDigitCharacters() {
        String input = "قیمت: ۱۰۰۰ تومان";
        String expected = "قیمت: 1000 تومان";
        assertEquals(expected, StringUtil.toEnglishNumbers(input));
    }
}