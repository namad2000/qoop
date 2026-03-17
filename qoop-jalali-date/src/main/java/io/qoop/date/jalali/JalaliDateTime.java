package io.qoop.date.jalali;

import java.io.Serializable;
import java.time.*;
import java.util.Objects;

import static io.qoop.date.jalali.JalaliConverter.toGregorian;
import static io.qoop.date.jalali.JalaliConverter.toJalali;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class JalaliDateTime implements Serializable {

    /**
     * The minimum supported {@code JalaliDateTime}, '1/01/01 00:00:00'.
     * This is the local date-time of midnight at the start of the minimum date.
     * This combines {@link JalaliDate#MIN} and {@link LocalTime#MIN}.
     * This could be used by an application as a "far past" date-time.
     */
    public static final JalaliDateTime MIN = JalaliDateTime.of(JalaliDate.MIN, LocalTime.MIN);

    /**
     * The maximum supported {@code JalaliDateTime}, 'Long.MAX_VALUE/12/29 23:59:59.999999999'.
     * This is the local date-time just before midnight at the end of the maximum date.
     * This combines {@link JalaliDate#MAX} and {@link LocalTime#MAX}.
     * This could be used by an application as a "far future" date-time.
     */
    public static final JalaliDateTime MAX = JalaliDateTime.of(JalaliDate.MAX, LocalTime.MAX);

    /**
     * The date part.
     */
    private final JalaliDate date;

    /**
     * The time part.
     */
    private final LocalTime time;

    //-----------------------------------------------------------------------

    /**
     * Obtains the current date from the LocalDate.now() in the default time-zone.
     *
     * @return the current date using the LocalDate.now() and default time-zone, not null
     */
    public static JalaliDateTime now() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return toJalaliDateTime(localDateTime);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains the current date from the LocalDate.now(ZoneId) in the specified time-zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current date using the LocalDate.now(ZoneId), not null
     */
    public static JalaliDateTime now(ZoneId zone) {
        LocalDateTime localDateTime = LocalDateTime.now(zone);
        return toJalaliDateTime(localDateTime);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliDateTime} from year, month,
     * day, hour, minute, second and nanosecond.
     * <p>
     * This returns a {@code JalaliDateTime} with the specified year, month,
     * day-of-month, hour, minute, second and nanosecond.
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     *
     * @param year         the year to represent, from 1 to Long.MAX_VALUE
     * @param month        the month-of-year to represent, from 1 (Farvardin) to 12 (Ordibehesht)
     * @param dayOfMonth   the day-of-month to represent, from 1 to 31
     * @param hour         the hour-of-day to represent, from 0 to 23
     * @param minute       the minute-of-hour to represent, from 0 to 59
     * @param second       the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond the nano-of-second to represent, from 0 to 999,999,999
     * @return the jalali date-time, not null
     * @throws DateTimeException and {@link JalaliDateTimeException} if the value of any field is out of range,
     *                           or if the day-of-month is invalid for the month-year
     */
    public static JalaliDateTime of(long year, short month, short dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        JalaliDate date = JalaliDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);

        return of(date, time);
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliDateTime} from a date and time.
     *
     * @param date the jalali date, not null
     * @param time the local time, not null
     * @return the jalali date-time, not null
     */
    public static JalaliDateTime of(JalaliDate date, LocalTime time) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(time, "time");

        return new JalaliDateTime(date, time);
    }

    //-----------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param date the date part of the date-time, validated not null
     * @param time the time part of the date-time, validated not null
     */
    private JalaliDateTime(JalaliDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Returns a copy of this date-time with the new date and time, checking
     * to see if a new object is in fact required.
     *
     * @param newDate the date of the new date-time, not null
     * @param newTime the time of the new date-time, not null
     * @return the date-time, not null
     */
    private static JalaliDateTime with(JalaliDate newDate, LocalTime newTime) {
        return new JalaliDateTime(newDate, newTime);
    }


    //-----------------------------------------------------------------------

    /**
     * Gets the {@code JalaliDate} part of this date-time.
     * <p>
     * This returns a {@code JalaliDate} with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    public JalaliDate toJalaliDate() {
        return date;
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
        return date.year();
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
        return date.monthValue();
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
        return date.month();
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
        return date.dayOfMonth();
    }


    //-----------------------------------------------------------------------

    /**
     * Gets the {@code LocalTime} part of this date-time.
     * <p>
     * This returns a {@code LocalTime} with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    public LocalTime toLocalTime() {
        return time;
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    public int hour() {
        return time.getHour();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    public int minute() {
        return time.getMinute();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    public int second() {
        return time.getSecond();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int nano() {
        return time.getNano();
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
        return date.dayOfWeek();
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of years added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd the years to add, may be negative
     * @return a {@code JalaliDateTime} based on this date with the years added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDateTime plusYears(long yearsToAdd) {
        JalaliDate newDate = date.plusYears(yearsToAdd);
        return with(newDate, time);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of months added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd the months to add, may be negative
     * @return a {@code JalaliDateTime} based on this date with the months added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDateTime plusMonths(int monthsToAdd) {
        JalaliDate newDate = date.plusMonths(monthsToAdd);
        return with(newDate, time);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of days added.
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd the days to add, may be negative
     * @return a {@code JalaliDateTime} based on this date with the days added, not null
     * @throws JalaliDateTimeException if the result exceeds the supported date range
     */
    public JalaliDateTime plusDays(long daysToAdd) {
        JalaliDate newDate = date.plusDays(daysToAdd);
        return with(newDate, time);
    }


    //-----------------------------------------------------------------------

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours the hours to add, may be negative
     * @return a {@code JalaliDateTime} based on this date-time with the hours added, not null
     * @throws DateTimeException and {@link JalaliDateTimeException} if the result exceeds the supported date range
     */
    public JalaliDateTime plusHours(long hours) {
        LocalDateTime localDateTime = toLocalDateTime();
        LocalDateTime newLocalDateTime = localDateTime.plusHours(hours);
        return toJalaliDateTime(newLocalDateTime);
    }

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes the minutes to add, may be negative
     * @return a {@code JalaliDateTime} based on this date-time with the minutes added, not null
     * @throws DateTimeException and {@link JalaliDateTimeException} if the result exceeds the supported date range
     */
    public JalaliDateTime plusMinutes(long minutes) {
        LocalDateTime localDateTime = toLocalDateTime();
        LocalDateTime newLocalDateTime = localDateTime.plusMinutes(minutes);
        return toJalaliDateTime(newLocalDateTime);
    }

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds the seconds to add, may be negative
     * @return a {@code JalaliDateTime} based on this date-time with the seconds added, not null
     * @throws DateTimeException and {@link JalaliDateTimeException} if the result exceeds the supported date range
     */
    public JalaliDateTime plusSeconds(long seconds) {
        LocalDateTime localDateTime = toLocalDateTime();
        LocalDateTime newLocalDateTime = localDateTime.plusSeconds(seconds);
        return toJalaliDateTime(newLocalDateTime);
    }

    /**
     * Returns a copy of this {@code JalaliDateTime} with the specified number of nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos the nanos to add, may be negative
     * @return a {@code JalaliDateTime} based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException and {@link JalaliDateTimeException} if the result exceeds the supported date range
     */
    public JalaliDateTime plusNanos(long nanos) {
        LocalDateTime localDateTime = toLocalDateTime();
        LocalDateTime newLocalDateTime = localDateTime.plusNanos(nanos);
        return toJalaliDateTime(newLocalDateTime);
    }

    //-----------------------------------------------------------------------

    /**
     * Outputs this date-time as a {@code String}, such as {@code 1404/12/26 10:52:30}.
     * <p>
     * The output will be one of the following JalaliDate formats:
     * <ul>
     * <li>{@code yyyy/MM/dd HH:mm}</li>
     * <li>{@code yyyy/MM/dd HH:mm:ss}</li>
     * <li>{@code yyyy/MM/dd HH:mm:ss.SSS}</li>
     * <li>{@code yyyy/MM/dd HH:mm:ss.SSSSSS}</li>
     * <li>{@code yyyy/MM/dd HH:mm:ss.SSSSSSSSS}</li>
     * </ul>
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this date-time, not null
     */
    @Override
    public String toString() {
        return date.toString() + " " + time.toString();
    }

    //-----------------------------------------------------------------------

    //TODO : Desc ...
    private static JalaliDateTime toJalaliDateTime(LocalDateTime newLocalDateTime) {
        JalaliDate jalaliDate = toJalali(newLocalDateTime.toLocalDate());
        LocalTime localTime = newLocalDateTime.toLocalTime();
        return with(jalaliDate, localTime);
    }

    //-----------------------------------------------------------------------

    //TODO : Desc ...
    private LocalDateTime toLocalDateTime() {
        LocalDate gregorian = toGregorian(date);
        return LocalDateTime.of(gregorian, time);
    }
}