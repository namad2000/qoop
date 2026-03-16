package io.qoop.date.jalali;

import static io.qoop.date.jalali.JalaliExceptionCode.INVALID_VALUE_MONTH_OF_YEAR;


/**
 * A month-of-year, such as 'Farvardin'.
 * <p>
 * {@code JalaliMonth} is an enum representing the 12 months of the year -
 * Farvardin, Ordibehesht, Khordad, Tir, Mordad, Shahrivar, Mehr, Aban, Azar, Day, Bahman and Esfand.
 * <p>
 * In addition to the textual enum name, each month-of-year has an {@code int} value.
 * The {@code int} value follows normal usage and the JalaliDate standard,
 * from 1 (Farvardin) to 12 (Esfand). It is recommended that applications use the enum
 * rather than the {@code int} value to ensure code clarity.
 * <p>
 * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code JalaliMonth}.
 * Use {@code value()} instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the month-of-year
 * concept defined exactly equivalent to the JalaliDate calendar system.
 *
 * @implSpec This is an immutable and thread-safe enum.
 * @since 0.0.1
 */
public enum JalaliMonth {

    /**
     * The singleton instance for the month of Farvardin with 31 days.
     * This has the numeric value of {@code 1}.
     */
    FARVARDIN("فروردین"),
    /**
     * The singleton instance for the month of Ordibehesht with 31 days.
     * This has the numeric value of {@code 2}.
     */
    ORDIBEHESHT("اردیبهشت"),
    /**
     * The singleton instance for the month of Khordad with 31 days.
     * This has the numeric value of {@code 3}.
     */
    KHORDAD("خرداد"),
    /**
     * The singleton instance for the month of Tir with 31 days.
     * This has the numeric value of {@code 4}.
     */
    TIR("تیر"),
    /**
     * The singleton instance for the month of Mordad with 31 days.
     * This has the numeric value of {@code 5}.
     */
    MORDAD("مرداد"),
    /**
     * The singleton instance for the month of Shahrivar with 31 days.
     * This has the numeric value of {@code 6}.
     */
    SHAHRIVAR("شهریور"),
    /**
     * The singleton instance for the month of Mehr with 30 days.
     * This has the numeric value of {@code 7}.
     */
    MEHR("مهر"),
    /**
     * The singleton instance for the month of Aban with 30 days.
     * This has the numeric value of {@code 8}.
     */
    ABAN("آبان"),
    /**
     * The singleton instance for the month of Azar with 30 days.
     * This has the numeric value of {@code 9}.
     */
    AZAR("آذر"),
    /**
     * The singleton instance for the month of Day with 30 days.
     * This has the numeric value of {@code 10}.
     */
    DAY("دی"),
    /**
     * The singleton instance for the month of Bahman with 30 days.
     * This has the numeric value of {@code 11}.
     */
    BAHMAN("بهمن"),
    /**
     * The singleton instance for the month of Esfand with 29 days, or 30 in a leap year.
     * This has the numeric value of {@code 12}.
     */
    ESFAND("اسفند");


    /**
     * Private persian name.
     */
    private final String persianName;

    /**
     * Private cache of all the constants.
     */
    private static final JalaliMonth[] ENUMS = JalaliMonth.values();


    JalaliMonth(String persianName) {
        this.persianName = persianName;
    }

    //-----------------------------------------------------------------------

    /**
     * Obtains an instance of {@code JalaliMonth} from an {@code int} value.
     * <p>
     * {@code JalaliMonth} is an enum representing the 12 months of the year.
     * This factory allows the enum to be obtained from the {@code int} value.
     * The {@code int} value follows the JalaliDate standard, from 1 (Farvardin) to 12 (Esfand).
     *
     * @param month the month-of-year to represent, from 1 (Farvardin) to 12 (Esfand)
     * @return the month-of-year, not null
     * @throws JalaliDateTimeException if the month-of-year is invalid
     */
    public static JalaliMonth of(int month) {
        if (month < 1 || month > 12) {
            throw JalaliDateTimeException.of(INVALID_VALUE_MONTH_OF_YEAR, month);
        }

        return ENUMS[month - 1];
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the persian-name-of-month {@code String} value.
     * <p>
     * The values are numbered following the JalaliDate standard,
     * from فروردین (Farvardin) to اسفند (Esfand).
     *
     * @return the persian-name-of-month, from فروردین (Farvardin) to اسفند (Esfand)
     */
    public String persianName() {
        return persianName;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the month-of-year {@code int} value.
     * <p>
     * The values are numbered following the JalaliDate standard,
     * from 1 (January) to 12 (December).
     *
     * @return the month-of-year, from 1 (January) to 12 (December)
     */
    public int value() {
        return ordinal() + 1;
    }

    //-----------------------------------------------------------------------

    /**
     * Returns the month-of-year that is the specified number of months after this one.
     * <p>
     * The calculation rolls around the end of the year from Esfand to Farvardin.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months the months to add, positive or negative
     * @return the resulting month, not null
     */
    public JalaliMonth plus(long months) {
        int amount = (int) (months % 12);
        return ENUMS[(ordinal() + (amount + 12)) % 12];
    }

    /**
     * Returns the month-of-year that is the specified number of months before this one.
     * <p>
     * The calculation rolls around the start of the year from Farvardin to Esfand.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months the months to subtract, positive or negative
     * @return the resulting month, not null
     */
    public JalaliMonth minus(long months) {
        return plus(-(months % 12));
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the length of this month in days.
     * <p>
     * This takes a flag to determine whether to return the length for a leap year or not.
     * <p>
     * Esfand has 29 days in a standard year and 30 days in a leap year.
     * Farvardin, Ordibehesht, Khordad, Tir, Mordad and Shahrivar have 31 days.
     * Mehr, Aban, Azar, Day and Bahman have 30 days.
     *
     * @param leapYear true if the length is required for a leap year
     * @return the length of this month in days, from 29 to 31
     */
    public short length(boolean leapYear) {
        return (short) switch (this) {
            case ESFAND -> (leapYear ? 30 : 29);
            case FARVARDIN, ORDIBEHESHT, KHORDAD, TIR, MORDAD, SHAHRIVAR -> 31;
            default -> 30;
        };
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the day-of-year corresponding to the first day of this month.
     * <p>
     * This returns the day-of-year that this month begins on, using the leap
     * year flag to determine the length of February.
     *
     * @return the day of year corresponding to the first day of this month, from 1 to 336
     */
    public int firstDayOfYear() {
        return switch (this) {
            case FARVARDIN -> 1;
            case ORDIBEHESHT -> (31) + 1;
            case KHORDAD -> (2 * 31) + 1;
            case TIR -> (3 * 31) + 1;
            case MORDAD -> (4 * 31) + 1;
            case SHAHRIVAR -> (5 * 31) + 1;
            case MEHR -> (6 * 31) + 1;
            case ABAN -> (6 * 31) + (30) + 1;
            case AZAR -> (6 * 31) + (2 * 30) + 1;
            case DAY -> (6 * 31) + (3 * 30) + 1;
            case BAHMAN -> (6 * 31) + (4 * 30) + 1;
            // otherwise (ESFAND)
            default -> (6 * 31) + (5 * 30) + 1;
        };
    }
}
