package io.qoop.utils.api.date.qoop;

import io.qoop.fault.handler.api.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class QoopConvertDateTest {

    @Test
    void gregorianToPersianConversionShouldWork() {
        LocalDateTime gregorianDate = LocalDateTime.of(2024, 3, 20, 0, 0);
        QoopParsiDateTime persianDate = QoopConvertDate.convertGregorianToPersian(gregorianDate);
        assertEquals(1403, persianDate.getYear());
        assertEquals(1, persianDate.getMonth());
        assertEquals(1, persianDate.getDay());
    }

    @Test
    void persianToGregorianConversionShouldWork() {
        QoopParsiDateTime persianDate = new QoopParsiDateTime(1403, 1, 1, 0, 0, 0, 0);
        LocalDateTime gregorianDate = QoopConvertDate.convertPersianToGregorian(persianDate);
        assertEquals(2024, gregorianDate.getYear());
        assertEquals(3, gregorianDate.getMonthValue());
        assertEquals(20, gregorianDate.getDayOfMonth());
    }

    @Test
    void leapYearShouldBeCorrect() {
        assertTrue(QoopConvertDate.isPersianLeapYear(1399));
        assertEquals(30, QoopConvertDate.getDaysInPersianMonth(1400, 12));
        assertFalse(QoopConvertDate.isPersianLeapYear(1400));
        assertEquals(29, QoopConvertDate.getDaysInPersianMonth(1401, 12));
    }

    @Test
    void epochConversionShouldWork() {
        LocalDateTime gregorianEpoch = LocalDateTime.of(622, 3, 22, 0, 0);
        QoopParsiDateTime persianEpoch = QoopConvertDate.convertGregorianToPersian(gregorianEpoch);
        assertEquals(1, persianEpoch.getYear());
        assertEquals(1, persianEpoch.getMonth());
        assertEquals(1, persianEpoch.getDay());
    }

    @Test
    void invalidGregorianDateShouldThrowException() {
        LocalDateTime invalidDate = LocalDateTime.of(622, 3, 21, 0, 0);
        assertThrows(DomainException.class, () -> QoopConvertDate.convertGregorianToPersian(invalidDate));
    }

    @Test
    void persianYear1402StartShouldConvertTo20230321() {
        QoopParsiDateTime persianDate = new QoopParsiDateTime(1402, 1, 1, 0, 0, 0, 0);
        LocalDateTime gregorianDate = QoopConvertDate.convertPersianToGregorian(persianDate);
        assertEquals(2023, gregorianDate.getYear());
        assertEquals(3, gregorianDate.getMonthValue());
        assertEquals(21, gregorianDate.getDayOfMonth());
    }

    @Test
    void persianYear1403EndShouldConvertTo20240319() {
        QoopParsiDateTime persianDate = new QoopParsiDateTime(1403, 12, 29, 0, 0, 0, 0);
        LocalDateTime gregorianDate = QoopConvertDate.convertPersianToGregorian(persianDate);
        assertEquals(2024, gregorianDate.getYear());
        assertEquals(3, gregorianDate.getMonthValue());
        assertEquals(19, gregorianDate.getDayOfMonth());
    }

    @Test
    void dayOfWeekShouldBeCorrect() {
        QoopParsiDateTime date = new QoopParsiDateTime(1402, 1, 1, 0, 0, 0, 0);
        assertEquals("شنبه", QoopConvertDate.getDayOfWeekName(date));
    }
}