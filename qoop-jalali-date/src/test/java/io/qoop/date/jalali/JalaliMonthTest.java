package io.qoop.date.jalali;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JalaliMonthTest {

    @Test
    void testFarvardin() {
        assertEquals("فروردین", JalaliMonth.FARVARDIN.persianName());
        assertEquals(1, JalaliMonth.FARVARDIN.value());
        assertEquals(1, JalaliMonth.FARVARDIN.firstDayOfYear());
        assertEquals(31, JalaliMonth.FARVARDIN.length(false));
        assertEquals(31, JalaliMonth.FARVARDIN.length(true));
    }

    @Test
    void testOrdibehesht() {
        assertEquals("اردیبهشت", JalaliMonth.ORDIBEHESHT.persianName());
        assertEquals(2, JalaliMonth.ORDIBEHESHT.value());
        assertEquals(32, JalaliMonth.ORDIBEHESHT.firstDayOfYear());
        assertEquals(31, JalaliMonth.ORDIBEHESHT.length(false));
        assertEquals(31, JalaliMonth.ORDIBEHESHT.length(true));
    }

    @Test
    void testKhordad() {
        assertEquals("خرداد", JalaliMonth.KHORDAD.persianName());
        assertEquals(3, JalaliMonth.KHORDAD.value());
        assertEquals(63, JalaliMonth.KHORDAD.firstDayOfYear());
        assertEquals(31, JalaliMonth.KHORDAD.length(false));
        assertEquals(31, JalaliMonth.KHORDAD.length(true));
    }

    @Test
    void testTir() {
        assertEquals("تیر", JalaliMonth.TIR.persianName());
        assertEquals(4, JalaliMonth.TIR.value());
        assertEquals(94, JalaliMonth.TIR.firstDayOfYear());
        assertEquals(31, JalaliMonth.TIR.length(false));
        assertEquals(31, JalaliMonth.TIR.length(true));
    }

    @Test
    void testMordad() {
        assertEquals("مرداد", JalaliMonth.MORDAD.persianName());
        assertEquals(5, JalaliMonth.MORDAD.value());
        assertEquals(125, JalaliMonth.MORDAD.firstDayOfYear());
        assertEquals(31, JalaliMonth.MORDAD.length(false));
        assertEquals(31, JalaliMonth.MORDAD.length(true));
    }

    @Test
    void testShahrivar() {
        assertEquals("شهریور", JalaliMonth.SHAHRIVAR.persianName());
        assertEquals(6, JalaliMonth.SHAHRIVAR.value());
        assertEquals(156, JalaliMonth.SHAHRIVAR.firstDayOfYear());
        assertEquals(31, JalaliMonth.SHAHRIVAR.length(false));
        assertEquals(31, JalaliMonth.SHAHRIVAR.length(true));
    }

    @Test
    void testMehr() {
        assertEquals("مهر", JalaliMonth.MEHR.persianName());
        assertEquals(7, JalaliMonth.MEHR.value());
        assertEquals(187, JalaliMonth.MEHR.firstDayOfYear());
        assertEquals(30, JalaliMonth.MEHR.length(false));
        assertEquals(30, JalaliMonth.MEHR.length(true));
    }

    @Test
    void testAban() {
        assertEquals("آبان", JalaliMonth.ABAN.persianName());
        assertEquals(8, JalaliMonth.ABAN.value());
        assertEquals(217, JalaliMonth.ABAN.firstDayOfYear());
        assertEquals(30, JalaliMonth.ABAN.length(false));
        assertEquals(30, JalaliMonth.ABAN.length(true));
    }

    @Test
    void testAzar() {
        assertEquals("آذر", JalaliMonth.AZAR.persianName());
        assertEquals(9, JalaliMonth.AZAR.value());
        assertEquals(247, JalaliMonth.AZAR.firstDayOfYear());
        assertEquals(30, JalaliMonth.AZAR.length(false));
        assertEquals(30, JalaliMonth.AZAR.length(true));
    }

    @Test
    void testDay() {
        assertEquals("دی", JalaliMonth.DAY.persianName());
        assertEquals(10, JalaliMonth.DAY.value());
        assertEquals(277, JalaliMonth.DAY.firstDayOfYear());
        assertEquals(30, JalaliMonth.DAY.length(false));
        assertEquals(30, JalaliMonth.DAY.length(true));
    }

    @Test
    void testBahman() {
        assertEquals("بهمن", JalaliMonth.BAHMAN.persianName());
        assertEquals(11, JalaliMonth.BAHMAN.value());
        assertEquals(307, JalaliMonth.BAHMAN.firstDayOfYear());
        assertEquals(30, JalaliMonth.BAHMAN.length(false));
        assertEquals(30, JalaliMonth.BAHMAN.length(true));
    }

    @Test
    void testEsfand() {
        assertEquals("اسفند", JalaliMonth.ESFAND.persianName());
        assertEquals(12, JalaliMonth.ESFAND.value());
        assertEquals(337, JalaliMonth.ESFAND.firstDayOfYear());
        assertEquals(29, JalaliMonth.ESFAND.length(false));
        assertEquals(30, JalaliMonth.ESFAND.length(true));
    }

    @Test
    void testOfValid() {
        assertEquals(JalaliMonth.FARVARDIN, JalaliMonth.of(1));
        assertEquals(JalaliMonth.ORDIBEHESHT, JalaliMonth.of(2));
        assertEquals(JalaliMonth.KHORDAD, JalaliMonth.of(3));
        assertEquals(JalaliMonth.TIR, JalaliMonth.of(4));
        assertEquals(JalaliMonth.MORDAD, JalaliMonth.of(5));
        assertEquals(JalaliMonth.SHAHRIVAR, JalaliMonth.of(6));
        assertEquals(JalaliMonth.MEHR, JalaliMonth.of(7));
        assertEquals(JalaliMonth.ABAN, JalaliMonth.of(8));
        assertEquals(JalaliMonth.AZAR, JalaliMonth.of(9));
        assertEquals(JalaliMonth.DAY, JalaliMonth.of(10));
        assertEquals(JalaliMonth.BAHMAN, JalaliMonth.of(11));
        assertEquals(JalaliMonth.ESFAND, JalaliMonth.of(12));
    }

    @Test
    void testOfInvalid() {
        JalaliDateTimeException exLower = assertThrows(JalaliDateTimeException.class, () -> JalaliMonth.of(0));
        assertEquals("JALALI_MONTH_01", exLower.getCode());

        JalaliDateTimeException exUpper = assertThrows(JalaliDateTimeException.class, () -> JalaliMonth.of(13));
        assertEquals("JALALI_MONTH_01", exUpper.getCode());
    }

    @Test
    void testPlus() {
        assertEquals(JalaliMonth.ORDIBEHESHT, JalaliMonth.FARVARDIN.plus(1));
        assertEquals(JalaliMonth.FARVARDIN, JalaliMonth.ESFAND.plus(1));
        assertEquals(JalaliMonth.ESFAND, JalaliMonth.FARVARDIN.plus(-1));
        assertEquals(JalaliMonth.FARVARDIN, JalaliMonth.FARVARDIN.plus(12));
    }

    @Test
    void testMinus() {
        assertEquals(JalaliMonth.ESFAND, JalaliMonth.FARVARDIN.minus(1));
        assertEquals(JalaliMonth.FARVARDIN, JalaliMonth.ESFAND.minus(-1));
        assertEquals(JalaliMonth.FARVARDIN, JalaliMonth.FARVARDIN.minus(12));
    }
}