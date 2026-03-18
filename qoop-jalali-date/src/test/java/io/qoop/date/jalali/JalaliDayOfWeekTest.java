package io.qoop.date.jalali;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JalaliDayOfWeekTest {

    @Test
    void testConstants() {
        assertEquals(7, JalaliDayOfWeek.NUMBER_OF_DAYS_IN_WEEK);
    }

    @Test
    void testValuesAndPersianNames() {
        assertEquals("شنبه", JalaliDayOfWeek.SHANBEH.persianName());
        assertEquals("یکشنبه", JalaliDayOfWeek.YEK_SHANBEH.persianName());
        assertEquals("دوشنبه", JalaliDayOfWeek.DO_SHANBEH.persianName());
        assertEquals("سه شنبه", JalaliDayOfWeek.SES_SHANBEH.persianName());
        assertEquals("چهارشنبه", JalaliDayOfWeek.CHAHAR_SHANBEH.persianName());
        assertEquals("پنج شنبه", JalaliDayOfWeek.PANJ_SHANBEH.persianName());
        assertEquals("جمعه", JalaliDayOfWeek.JOME.persianName());
    }

    @Test
    void testValue() {
        assertEquals(0, JalaliDayOfWeek.SHANBEH.value());
        assertEquals(1, JalaliDayOfWeek.YEK_SHANBEH.value());
        assertEquals(2, JalaliDayOfWeek.DO_SHANBEH.value());
        assertEquals(3, JalaliDayOfWeek.SES_SHANBEH.value());
        assertEquals(4, JalaliDayOfWeek.CHAHAR_SHANBEH.value());
        assertEquals(5, JalaliDayOfWeek.PANJ_SHANBEH.value());
        assertEquals(6, JalaliDayOfWeek.JOME.value());
    }

    @Test
    void testOfValid() {
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.of((short) 0));
        assertEquals(JalaliDayOfWeek.YEK_SHANBEH, JalaliDayOfWeek.of((short) 1));
        assertEquals(JalaliDayOfWeek.DO_SHANBEH, JalaliDayOfWeek.of((short) 2));
        assertEquals(JalaliDayOfWeek.SES_SHANBEH, JalaliDayOfWeek.of((short) 3));
        assertEquals(JalaliDayOfWeek.CHAHAR_SHANBEH, JalaliDayOfWeek.of((short) 4));
        assertEquals(JalaliDayOfWeek.PANJ_SHANBEH, JalaliDayOfWeek.of((short) 5));
        assertEquals(JalaliDayOfWeek.JOME, JalaliDayOfWeek.of((short) 6));
    }

    @Test
    void testOfInvalid_LowerBound() {
        JalaliDateTimeException ex = assertThrows(JalaliDateTimeException.class, () -> JalaliDayOfWeek.of(-1));
        assertEquals("JALALI_DAY_OF_WEEK_01", ex.getCode());
    }

    @Test
    void testOfInvalid_UpperBound() {
        JalaliDateTimeException ex = assertThrows(JalaliDateTimeException.class, () -> JalaliDayOfWeek.of(7));
        assertEquals("JALALI_DAY_OF_WEEK_01", ex.getCode());
    }

    @Test
    void testPlus_Positive() {
        assertEquals(JalaliDayOfWeek.YEK_SHANBEH, JalaliDayOfWeek.SHANBEH.plus(1));
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.JOME.plus(1)); // Roll over: 6 + 1 = 7 -> 0
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.SHANBEH.plus(7)); // Full week
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.SHANBEH.plus(14)); // 2 weeks
    }

    @Test
    void testPlus_Negative() {
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.YEK_SHANBEH.plus(-1)); // 1 - 1 = 0
        assertEquals(JalaliDayOfWeek.JOME, JalaliDayOfWeek.SHANBEH.plus(-1)); // Roll back: 0 - 1 = -1 -> 6
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.SHANBEH.plus(-7)); // Full week back
    }

    @Test
    void testMinus_Positive() {
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.YEK_SHANBEH.minus(1));
        assertEquals(JalaliDayOfWeek.JOME, JalaliDayOfWeek.SHANBEH.minus(1)); // Roll back
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.SHANBEH.minus(7));
    }

    @Test
    void testMinus_Negative() {
        assertEquals(JalaliDayOfWeek.YEK_SHANBEH, JalaliDayOfWeek.SHANBEH.minus(-1));
        assertEquals(JalaliDayOfWeek.SHANBEH, JalaliDayOfWeek.JOME.minus(-1)); // Roll over
    }
}