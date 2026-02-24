package io.qoop.utils.api.date.qoop;

import io.qoop.fault.handler.api.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QoopParsiDateTimeTest {

    @Test
    void validDateShouldNotThrowException() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 12, 30, 45, 123);
        assertEquals(1402, date.getYear());
        assertEquals(1, date.getMonth());
        assertEquals(1, date.getDay());
        assertEquals(12, date.getHour());
        assertEquals(30, date.getMinute());
        assertEquals(45, date.getSecond());
        assertEquals(123, date.getMicrosecond());
    }

    @Test
    void invalidYearShouldThrowException() {
        assertThrows(DomainException.class, () -> new QoopParsiDateTime(0, 1, 1, 0, 0, 0, 0));
    }

    @Test
    void invalidMonthShouldThrowException() {
        assertThrows(DomainException.class, () -> new QoopParsiDateTime(1402, 0, 1, 0, 0, 0, 0));
        assertThrows(DomainException.class, () -> new QoopParsiDateTime(1402, 13, 1, 0, 0, 0, 0));
    }

    @Test
    void invalidDayShouldThrowException() {
        assertThrows(DomainException.class, () -> new QoopParsiDateTime(1402, 1, 32, 0, 0, 0, 0)); // Farvardin has 31 days
        assertThrows(DomainException.class, () -> new QoopParsiDateTime(1400, 12, 31, 0, 0, 0, 0)); // Esfand in leap year has 30 days
    }

    @Test
    void addYearsShouldWorkCorrectly() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 0, 0, 0, 0);
        QoopParsiDateTime newDate = date.addYears(1);
        assertEquals(1403, newDate.getYear());
        assertEquals(1, newDate.getMonth());
        assertEquals(1, newDate.getDay());
    }

    @Test
    void addMonthsShouldWorkCorrectly() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 0, 0, 0, 0);
        QoopParsiDateTime newDate = date.addMonths(13);
        assertEquals(1403, newDate.getYear());
        assertEquals(2, newDate.getMonth());
        assertEquals(1, newDate.getDay());
    }

    @Test
    void addDaysShouldWorkCorrectly() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 0, 0, 0, 0);
        QoopParsiDateTime newDate = date.addDays(31);
        assertEquals(1402, newDate.getYear());
        assertEquals(2, newDate.getMonth());
        assertEquals(1, newDate.getDay());
    }

    @Test
    void toStringShouldFormatCorrectly() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 12, 30, 45, 123);
        assertEquals("1402-01-01 12:30:45.123", date.toString());
    }

    @Test
    void toPersianStringShouldFormatCorrectly() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 12, 30, 45, 123);
        assertEquals("1402/01/01 12:30:45.123", date.toPersianString());
    }

    @Test
    void nowShouldReturnCurrentPersianDate() {
        QoopParsiDateTime now = QoopParsiDateTime.now();
        assertTrue(now.getYear() >= 1);
        assertTrue(now.getMonth() >= 1 && now.getMonth() <= 12);
        assertTrue(now.getDay() >= 1 && now.getDay() <= 31);
    }
}