package io.qoop.utils.api.date.qoop;

import io.qoop.fault.handler.api.exception.DomainException;
import lombok.Getter;

import static io.qoop.utils.api.date.qoop.QoopDateExceptionCode.*;

public class QoopParsiDateTime {
    private final long persianYear;
    private final int persianMonth;
    private final int persianDay;

    @Getter
    private final int hour;
    @Getter
    private final int minute;
    @Getter
    private final int second;
    @Getter
    private final int microsecond;

    public QoopParsiDateTime(long persianYear, int persianMonth, int persianDay, int hour, int minute, int second, int microsecond) {
        // اعتبارسنجی سال
        if (persianYear <= 0) {
            throw DomainException.of(INVALID_PERSIAN_YEAR_RANGE);
        }
        // اعتبارسنجی ماه
        if (persianMonth < 1 || persianMonth > 12) {
            throw DomainException.of(INVALID_PERSIAN_MONTH_RANGE);
        }
        // اعتبارسنجی روز
        int maxDays = QoopConvertDate.getDaysInPersianMonth(persianYear, persianMonth);
        if (persianDay < 1 || persianDay > maxDays) {
            throw DomainException.withParams(INVALID_PERSIAN_DAY_RANGE, maxDays);
        }
        // اعتبارسنجی ساعت
        if (hour < 0 || hour > 23) {
            throw DomainException.of(INVALID_HOUR_RANGE);
        }
        // اعتبارسنجی دقیقه
        if (minute < 0 || minute > 59) {
            throw DomainException.of(INVALID_MINUTE_RANGE);
        }
        // اعتبارسنجی ثانیه
        if (second < 0 || second > 59) {
            throw DomainException.of(INVALID_SECOND_RANGE);
        }
        // اعتبارسنجی میلی‌ثانیه
        if (microsecond < 0 || microsecond > 999999) {
            throw DomainException.of(INVALID_MICROSECOND_RANGE);
        }

        this.persianYear = persianYear;
        this.persianMonth = persianMonth;
        this.persianDay = persianDay;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.microsecond = microsecond;
    }

    private String padWithZero(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    @Override
    public String toString() {
        return String.format("%d-%s-%s %s:%s:%s.%s",
                persianYear, padWithZero(persianMonth), padWithZero(persianDay),
                padWithZero(hour), padWithZero(minute), padWithZero(second),
                padWithZero(microsecond));
    }

    public String toPersianString() {
        return String.format("%d/%s/%s %s:%s:%s.%s",
                persianYear, padWithZero(persianMonth), padWithZero(persianDay),
                padWithZero(hour), padWithZero(minute), padWithZero(second),
                padWithZero(microsecond));
    }

    public QoopParsiDateTime addYears(int yearsToAdd) {
        return new QoopParsiDateTime(persianYear + yearsToAdd, persianMonth, persianDay, hour, minute, second, microsecond);
    }

    public QoopParsiDateTime addMonths(int monthsToAdd) {
        if (monthsToAdd == 0) return this;

        long tempYear = Math.abs(monthsToAdd) / 12;
        int tempMonth = Math.abs(monthsToAdd) % 12;

        if (monthsToAdd > 0) {
            tempMonth += persianMonth;
            if (tempMonth > 12) {
                tempYear += tempMonth / 12;
                tempMonth %= 12;
            }
        } else {
            tempMonth = persianMonth - tempMonth;
            if (tempMonth <= 0) {
                tempMonth += 12;
                tempYear++;
            }
            tempYear = -tempYear;
        }

        QoopParsiDateTime temp = addYears((int) tempYear);
        return new QoopParsiDateTime(temp.persianYear, tempMonth, persianDay, hour, minute, second, microsecond);
    }

    public QoopParsiDateTime addDays(int daysToAdd) {
        return QoopConvertDate.convertGregorianToPersian(QoopConvertDate.convertPersianToGregorian(this).plusDays(daysToAdd));
    }

    public QoopParsiDateTime addHours(int hoursToAdd) {
        return QoopConvertDate.convertGregorianToPersian(QoopConvertDate.convertPersianToGregorian(this).plusHours(hoursToAdd));
    }

    public QoopParsiDateTime addMinutes(int minutesToAdd) {
        return QoopConvertDate.convertGregorianToPersian(QoopConvertDate.convertPersianToGregorian(this).plusMinutes(minutesToAdd));
    }

    public QoopParsiDateTime addSeconds(int secondsToAdd) {
        return QoopConvertDate.convertGregorianToPersian(QoopConvertDate.convertPersianToGregorian(this).plusSeconds(secondsToAdd));
    }

    public QoopParsiDateTime addMicroseconds(int microsecondsToAdd) {
        return QoopConvertDate.convertGregorianToPersian(QoopConvertDate.convertPersianToGregorian(this).plusNanos(microsecondsToAdd * 1000L));
    }

    public static QoopParsiDateTime getMaxValue() {
        return new QoopParsiDateTime(9223372036854775807L, 12, 29, 23, 59, 59, 999999);
    }

    public static QoopParsiDateTime getMinValue() {
        return new QoopParsiDateTime(1, 1, 1, 0, 0, 0, 0);
    }

    public String getMonthName() {
        return QoopConvertDate.PERSIAN_MONTH_NAMES[persianMonth];
    }

    public int getDayOfWeekNumber() {
        return QoopConvertDate.getDayOfWeekNumber(this);
    }

    public String getDayOfWeekName() {
        return QoopConvertDate.getDayOfWeekName(this);
    }

    public int getDay() {
        return persianDay;
    }

    public int getMonth() {
        return persianMonth;
    }

    public long getYear() {
        return persianYear;
    }

    public static QoopParsiDateTime now() {
        return QoopConvertDate.getPersianDate();
    }
}