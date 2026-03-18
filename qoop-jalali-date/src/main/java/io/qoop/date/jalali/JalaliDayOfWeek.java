package io.qoop.date.jalali;

import static io.qoop.date.jalali.JalaliExceptionCode.INVALID_VALUE_DAYOFWEEK;

/**
 * A day-of-week, such as 'SeShanbeh'.
 * <p>
 * {@code JalaliDayOfWeek} is an enum representing the 7 days of the week -
 * Shanbeh, Yekshanbeh, Doshanbeh, SeShanbeh, Chaharshanbeh, PanjShanbe and Jome.
 * <p>
 * In addition to the textual enum name, each day-of-week has an {@code short} value.
 * The {@code short} value follows the JalaliDate standard, from 0 (Shanbeh) to 6 (Jome).
 * It is recommended that applications use the enum rather than the {@code short} value
 * to ensure code clarity.
 * <p>
 * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code JalaliDayOfWeek}.
 * Use {@code value()} instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the day-of-week
 * concept defined exactly equivalent to the ISO calendar system.
 * <p>
 *
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 * <p>
 * @implSpec This is an immutable and thread-safe enum.
 * @since 0.0.1
 */
public enum JalaliDayOfWeek {

    /**
     * The singleton instance for the day-of-week of Shanbeh.
     * This has the numeric value of {@code 0}.
     */
    SHANBEH("شنبه"),

    /**
     * The singleton instance for the day-of-week of Yekshanbeh.
     * This has the numeric value of {@code 1}.
     */
    YEK_SHANBEH("یکشنبه"),

    /**
     * The singleton instance for the day-of-week of Doshanbeh.
     * This has the numeric value of {@code 2}.
     */
    DO_SHANBEH("دوشنبه"),

    /**
     * The singleton instance for the day-of-week of SeShanbeh.
     * This has the numeric value of {@code 3}.
     */
    SES_SHANBEH("سه شنبه"),

    /**
     * The singleton instance for the day-of-week of Chaharshanbeh.
     * This has the numeric value of {@code 4}.
     */
    CHAHAR_SHANBEH("چهارشنبه"),

    /**
     * The singleton instance for the day-of-week of PanjShanbe.
     * This has the numeric value of {@code 5}.
     */
    PANJ_SHANBEH("پنج شنبه"),

    /**
     * The singleton instance for the day-of-week of Jome.
     * This has the numeric value of {@code 6}.
     */
    JOME("جمعه");


    /**
     * The number-of-days-in-week.
     */
    public static final short NUMBER_OF_DAYS_IN_WEEK = 7;

    /**
     * Private persian name.
     */
    private final String persianName;

    /**
     * Private cache of all the constants.
     */
    private static final JalaliDayOfWeek[] ENUMS = JalaliDayOfWeek.values();


    JalaliDayOfWeek(String persianName) {
        this.persianName = persianName;
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliDayOfWeek} from an {@code int} value.
     * <p>
     * {@code JalaliDayOfWeek} is an enum representing the 7 days of the week.
     * This factory allows the enum to be obtained from the {@code int} value.
     * The {@code int} value follows the JalaliDate standard, from 0 (Shanbeh) to 6 (Jome).
     *
     * @param dayOfWeek the day-of-week to represent, from 0 (Shanbeh) to 6 (Jome)
     * @return the day-of-week singleton, not null
     * @throws JalaliDateTimeException if the day-of-week is invalid
     */
    public static JalaliDayOfWeek of(int dayOfWeek) {
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw JalaliDateTimeException.withParams(INVALID_VALUE_DAYOFWEEK, dayOfWeek);
        }
        return ENUMS[dayOfWeek];
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the persian-name-of-day-of-week {@code String} value.
     * <p>
     * The values are numbered following the JalaliDate standard,
     * from شنبه (Shanbeh) to جمعه (Jome).
     *
     * @return the persian-name-of-month, from شنبه (Shanbeh) to جمعه (Jome)
     */
    public String persianName() {
        return persianName;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the day-of-week {@code int} value.
     * <p>
     * The values are numbered following the JalaliDate standard, from 0 (Shanbeh) to 6 (Jome).
     *
     * @return the day-of-week, from 0 (Shanbeh) to 6 (Jome)
     */
    public int value() {
        return ordinal();
    }

    //-----------------------------------------------------------------------

    /**
     * Returns the day-of-week that is the specified number of days after this one.
     * <p>
     * The calculation rolls around the end of the week from Shanbeh to Jome.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days the days to add, positive or negative
     * @return the resulting day-of-week, not null
     */
    public JalaliDayOfWeek plus(long days) {
        int amount = (int) (days % 7);
        return ENUMS[(ordinal() + (amount + 7)) % 7];
    }

    /**
     * Returns the day-of-week that is the specified number of days before this one.
     * <p>
     * The calculation rolls around the start of the year from Jome to Shanbeh.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days the days to subtract, positive or negative
     * @return the resulting day-of-week, not null
     */
    public JalaliDayOfWeek minus(long days) {
        return plus(-(days % 7));
    }
}