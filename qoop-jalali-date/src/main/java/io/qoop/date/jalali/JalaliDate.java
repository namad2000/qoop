package io.qoop.date.jalali;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import static io.qoop.date.jalali.JalaliConverter.*;
import static io.qoop.date.jalali.JalaliExceptionCode.*;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class JalaliDate implements Serializable {
    /**
     * The minimum supported {@code JalaliDate}, '1/01/01'.
     * This could be used by an application as a "far past" date.
     */
    public static final JalaliDate MIN = JalaliDate.of(1L, (short) 1, (short) 1);

    /**
     * The maximum supported {@code LocalDate}, 'Long.MAX_VALUE/12/29'.
     * This could be used by an application as a "far future" date.
     */
    public static final JalaliDate MAX = JalaliDate.of(Long.MAX_VALUE, (short) 12, (short) 29);

    /**
     * The year.
     */
    private final long year;

    /**
     * The month-of-year.
     */
    private final short month;

    /**
     * The day-of-month.
     */
    private final short day;

    //-----------------------------------------------------------------------

    /**
     * Obtains the current date from the LocalDate.now() in the default time-zone.
     *
     * @return the current date using the LocalDate.now() and default time-zone, not null
     */
    public static JalaliDate now() {
        return JalaliConverter.now();
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains the current date from the LocalDate.now(ZoneId) in the specified time-zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current date using the LocalDate.now(ZoneId), not null
     */
    public static JalaliDate now(ZoneId zone) {
        return JalaliConverter.now(zone);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliDate} from a year, month and day.
     * <p>
     * This returns a {@code JalaliDate} with the specified year, month and day-of-month.
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     *
     * @param year       the year to represent, from 1 to Long.MAX_VALUE
     * @param month      the month-of-year to represent, from 1 (Farvardin) to 12 (Esfand)
     * @param dayOfMonth the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws JalaliDateTimeException if the value of any field is out of range,
     *                                 or if the day-of-month is invalid for the month-year
     */
    public static JalaliDate of(long year, short month, short dayOfMonth) {
        return create(year, month, dayOfMonth);
    }

    //-----------------------------------------------------------------------

    /**
     * Creates a JalaliDate from the year, month and day fields.
     *
     * @param year       the year to represent, from 1 to Long.MAX_VALUE
     * @param month      the month-of-year to represent, from 1 (Farvardin) to 12 (Esfand)
     * @param dayOfMonth the day-of-month to represent, from 1 to 31
     * @return the local date, not null
     * @throws JalaliDateTimeException if the value of any field is out of range,
     *                                 or if the day-of-month is invalid for the month-yea
     */
    private static JalaliDate create(long year, short month, short dayOfMonth) {
        if (year <= 0) {
            throw JalaliDateTimeException.of(INVALID_PERSIAN_YEAR);
        } else if ((month > 12) || (month < 1)) {
            throw JalaliDateTimeException.of(INVALID_PERSIAN_MONTH_NUMBER);
        } else {
            JalaliMonth jalaliMonth = JalaliMonth.of(month);
            short length = jalaliMonth.length(isLeapYear(year));

            if ((dayOfMonth < 1) || (dayOfMonth > length)) {
                throw JalaliDateTimeException.of(INVALID_PERSIAN_DAY_RANGE, length);
            }
        }

        return new JalaliDate(year, month, dayOfMonth);
    }

    //-----------------------------------------------------------------------

    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from 1 to Long.MAX_VALUE
     * @param month the month-of-year to represent, not null
     * @param day   the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private JalaliDate(long year, short month, short day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code long} value for the year.
     * <p>
     *
     * @return the year, from 1 to Long.MAX_VALUE
     */
    public long year() {
        return year;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the month-of-year field from 1 to 12.
     * <p>
     * This method returns the month as an {@code short} from 1 to 12.
     * Application code is frequently clearer if the enum {@link JalaliMonth}
     * is used by calling {@link #month()}.
     *
     * @return the month-of-year, from 1 to 12
     * @see #month()
     */
    public short monthValue() {
        return month;
    }

    /**
     * Gets the month-of-year field using the {@code JalaliMonth} enum.
     * <p>
     * This method returns the enum {@link JalaliMonth} for the month.
     * This avoids confusion as to what {@code short} values mean.
     * If you need access to the primitive {@code short} value then the enum
     * provides the {@link JalaliMonth#value()} () int value}.
     *
     * @return the month-of-year, not null
     * @see #monthValue()
     */
    public JalaliMonth month() {
        return JalaliMonth.of(month);
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code short} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    public short dayOfMonth() {
        return day;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
     * <p>
     * This method returns the enum {@link JalaliDayOfWeek} for the day-of-week.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link JalaliDayOfWeek#value()} () int value}.
     * <p>
     * Additional information can be obtained from the {@code DayOfWeek}.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    public JalaliDayOfWeek dayOfWeek() {
        return dayOfWeekNumber(this);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDate} with the specified number of years added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd the years to add, may be negative
     * @return a {@code JalaliDate} based on this date with the years added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }

        return JalaliDate.of(year + yearsToAdd, month, day);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDate} with the specified number of months added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd the months to add, may be negative
     * @return a {@code JalaliDate} based on this date with the months added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDate plusMonths(int monthsToAdd) {

        if (monthsToAdd == 0) {
            return this;
        }

        // در زیر تعداد سال یا ماهی که قرار است کم یا زیاد شود بدست می آید
        long tempYear = Math.abs(monthsToAdd) / 12;
        short tempMonth = (short) (Math.abs(monthsToAdd) % 12);

        // در زیر اگر ورودی صفر باشد هیچ عملی انجام نمی شود ولی اگر منفی یا مثبت باشد با توجه به آن ماه و سال بدست می آید
        if (monthsToAdd > 0) {
            tempMonth = (short) (month + tempMonth);
            if (tempMonth > 12) {
                tempYear += (tempMonth / 12); // حداکثر یکسال اضافه می شود
                tempMonth = (short) (tempMonth % 12);
            }
        } else {
            tempMonth = (short) (month - tempMonth);
            if (tempMonth <= 0) {
                tempMonth = (short) (12 + tempMonth); // با این دلیل با 12 جمع می شود زیرا عدد منقی است و خروجی عددی مثبت و ماه مورد نظر است
                tempYear += 1; // در زیر .سال با تابع مربوط به خود کم یا زیاد می شود و جمع با ۱ یعنی یکسال دیگر برای کم شدن اضافه می شود
            }
            tempYear = -tempYear; // در اینجا منفی می شود زیرا می خواهیم اگر با تابع حساب شد کم کند
        }

        return JalaliDate.of(this.plusYears(tempYear).year, tempMonth, day);

    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDate} with the specified number of days added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd the days to add, may be negative
     * @return a {@code JalaliDate} based on this date with the days added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }

        LocalDate gregorian = toGregorian(this);
        LocalDate updatedGregorian = gregorian.plus(Duration.ofDays(daysToAdd));
        return toJalali(updatedGregorian);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliDate} from a localDate object.
     *
     * @param localDate the temporal object to convert, not null
     * @return the jalali date, not null
     * @throws JalaliDateTimeException if unable to convert to a {@code LocalDate}
     */
    public static JalaliDate from(LocalDate localDate) {
        return toJalali(localDate);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code LocalDate}.
     *
     * @return the local date, not null
     * @throws JalaliDateTimeException if unable to convert to a {@code JalaliDate}
     */
    public LocalDate toLocalDate() {
        return toGregorian(this);
    }

    //-----------------------------------------------------------------------

    /**
     * Outputs this JalaliDate as a {@code String}, such as {@code 1404/12/25}.
     * <p>
     * The output will be in the JalaliDate format {@code yyyy/MM/dd}.
     *
     * @return a string representation of this date, not null
     */
    @Override
    public String toString() {
        return String.valueOf(this.year) + '/' +
                to2Digits(String.valueOf(this.month)) + '/' +
                to2Digits(String.valueOf(this.day));
    }

    //-----------------------------------------------------------------------

    //TODO : Desc ...
    private String to2Digits(String str) {
        if (str.length() < 2) {
            str = '0' + str;
        }
        return str;
    }
}