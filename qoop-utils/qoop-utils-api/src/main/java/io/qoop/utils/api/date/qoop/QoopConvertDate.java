package io.qoop.utils.api.date.qoop;

import io.qoop.fault.handler.api.exception.DomainException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static io.qoop.utils.api.date.qoop.QoopDateExceptionCode.*;

public class QoopConvertDate {
    // ثابت‌های تقویم خورشیدی
    public static final long BIG_AGE_CYCLE_YEARS = 2820;
    public static final int TWELVE_EIGHT_CYCLE_YEARS = 128;
    public static final int THIRTEEN_TWO_CYCLE_YEARS = 132;
    public static final int TWENTY_NINE_CYCLE_YEARS = 29;
    public static final int THIRTY_THREE_CYCLE_YEARS = 33;
    public static final int THIRTY_SEVEN_CYCLE_YEARS = 37;
    public static final int FIVE_CYCLE_YEARS = 5;
    public static final int FOUR_CYCLE_YEARS = 4;
    public static final int TWELVE_EIGHT_CIRCUITS = 21;
    public static final int THIRTEEN_TWO_CIRCUITS = 1;
    public static final long TWELVE_EIGHT_CIRCUITS_TOTAL_YEARS = TWELVE_EIGHT_CIRCUITS * TWELVE_EIGHT_CYCLE_YEARS;
    public static final long THIRTEEN_TWO_CIRCUITS_TOTAL_YEARS = THIRTEEN_TWO_CIRCUITS * THIRTEEN_TWO_CYCLE_YEARS;
    public static final long YEARS_BEFORE_THIRTY_SEVEN_CYCLE = TWENTY_NINE_CYCLE_YEARS + (2 * THIRTY_THREE_CYCLE_YEARS);
    public static final int LEAP_YEARS_IN_TWELVE_EIGHT_CYCLE = 31;
    public static final int LEAP_YEARS_IN_THIRTEEN_TWO_CYCLE = 32;
    public static final int LEAP_YEARS_IN_TWENTY_NINE_CYCLE = 7;
    public static final int LEAP_YEARS_IN_THIRTY_THREE_CYCLE = 8;
    public static final int LEAP_YEARS_IN_THIRTY_SEVEN_CYCLE = 9;
    public static final int LEAP_YEARS_IN_FIVE_OR_FOUR_CYCLE = 1;
    public static final long LEAP_YEARS_IN_TWELVE_EIGHT_CIRCUITS = TWELVE_EIGHT_CIRCUITS * LEAP_YEARS_IN_TWELVE_EIGHT_CYCLE;
    public static final long LEAP_YEARS_IN_THIRTEEN_TWO_CIRCUITS = THIRTEEN_TWO_CIRCUITS * LEAP_YEARS_IN_THIRTEEN_TWO_CYCLE;
    public static final long TOTAL_LEAP_YEARS_IN_BIG_AGE_CYCLE = LEAP_YEARS_IN_TWELVE_EIGHT_CIRCUITS + LEAP_YEARS_IN_THIRTEEN_TWO_CIRCUITS;
    public static final long YEARS_TO_SKIP = 2346;
    public static final long LEAP_YEARS_TO_SKIP = ((YEARS_TO_SKIP / TWELVE_EIGHT_CYCLE_YEARS) * LEAP_YEARS_IN_TWELVE_EIGHT_CYCLE) + LEAP_YEARS_IN_TWENTY_NINE_CYCLE + 3;
    public static final int DAYS_IN_NORMAL_YEAR = 365;
    public static final int DAYS_IN_LEAP_YEAR = 366;
    public static final long DAYS_TO_SKIP = (YEARS_TO_SKIP * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_TO_SKIP;
    public static final long DAYS_IN_BIG_AGE_CYCLE = (BIG_AGE_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + TOTAL_LEAP_YEARS_IN_BIG_AGE_CYCLE;
    public static final long DAYS_IN_TWELVE_EIGHT_CYCLE = (TWELVE_EIGHT_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_TWELVE_EIGHT_CYCLE;
    public static final long DAYS_IN_THIRTEEN_TWO_CYCLE = (THIRTEEN_TWO_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_THIRTEEN_TWO_CYCLE;
    public static final long DAYS_IN_TWENTY_NINE_CYCLE = (TWENTY_NINE_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_TWENTY_NINE_CYCLE;
    public static final long DAYS_IN_THIRTY_THREE_CYCLE = (THIRTY_THREE_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_THIRTY_THREE_CYCLE;
    public static final long DAYS_IN_THIRTY_SEVEN_CYCLE = (THIRTY_SEVEN_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_THIRTY_SEVEN_CYCLE;
    public static final long DAYS_IN_FIVE_CYCLE = (FIVE_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_FIVE_OR_FOUR_CYCLE;
    public static final long DAYS_IN_FOUR_CYCLE = (FOUR_CYCLE_YEARS * DAYS_IN_NORMAL_YEAR) + LEAP_YEARS_IN_FIVE_OR_FOUR_CYCLE;
    public static final int[] LEAP_YEAR_PATTERN = {0, 5, 9, 13, 17, 21, 25, 29, 33, 37};
    public static final String[] PERSIAN_MONTH_NAMES = {
            "فروردین", "ارديبهشت", "خرداد", "تير", "امرداد", "شهريور",
            "مهر", "آبان", "آذر", "دى", "بهمن", "اسفند"
    };
    public static final String[] DAY_OF_WEEK_NAMES = {
            "شنبه", "يكشنبه", "دوشنبه", "سه شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    };

    public static QoopParsiDateTime convertGregorianToPersian(LocalDateTime gregorianDate) {
        if (gregorianDate.isBefore(LocalDateTime.of(622, 3, 22, 0, 0))) {
            throw DomainException.of(INVALID_GREGORIAN_DATE_RANGE);
        }

        long daysSinceEpoch = ChronoUnit.DAYS.between(LocalDateTime.of(622, 3, 22, 0, 0), gregorianDate);
        long[] yearAndDay = convertDaysToPersianYear(daysSinceEpoch);
        return convertDaysToPersianDate(yearAndDay[0], (int) yearAndDay[1]);
    }

    public static LocalDateTime convertPersianToGregorian(QoopParsiDateTime persianDate) {
        long totalLeapYears = getTotalLeapYearsUpTo(persianDate.getYear()) - LEAP_YEARS_TO_SKIP;
        long days = (persianDate.getYear() - 1) * DAYS_IN_NORMAL_YEAR + totalLeapYears;

        int extraDays = (persianDate.getMonth() > 6) ? 6 : (persianDate.getMonth() - 1);
        int otherDays = (persianDate.getMonth() - 1) * 30 + extraDays;
        days += (otherDays + persianDate.getDay() - 1);

        return LocalDateTime.of(622, 3, 22, 0, 0).plusDays(days)
                .withHour(persianDate.getHour())
                .withMinute(persianDate.getMinute())
                .withSecond(persianDate.getSecond())
                .withNano(persianDate.getMicrosecond() * 1000);
    }

    private static QoopParsiDateTime convertDaysToPersianDate(long year, int remainingDays) {
        int daysInFirstHalfOfYear = 6 * 31;
        remainingDays++;
        boolean isYearEndDay = (remainingDays / DAYS_IN_LEAP_YEAR == 1) && !isPersianLeapYear(year);
        int daysInMonth = (remainingDays / daysInFirstHalfOfYear == 0) ? 31 : 30;
        int halfOfYear = (int) (remainingDays / daysInFirstHalfOfYear);
        remainingDays -= daysInFirstHalfOfYear * halfOfYear;
        int month = (int) Math.ceil(remainingDays / (double) daysInMonth) + (halfOfYear * 6);
        int isEndOfMonth = (int) ((remainingDays - ((Math.ceil(month) - 1) * daysInMonth)) / daysInMonth);
        remainingDays = (int) ((remainingDays % daysInMonth) + (isEndOfMonth * daysInMonth));

        if (isYearEndDay) {
            remainingDays -= (30 - 1);
            month -= 11;
            year++;
        }

        return new QoopParsiDateTime(year, month, remainingDays, 0, 0, 0, 0);
    }

    private static long[] convertDaysToPersianYear(long daysSinceEpoch) {
        long eraIndex = getEraIndexForBigAgeCycle(daysSinceEpoch);
        long yearsFromEras = (eraIndex - 1) * BIG_AGE_CYCLE_YEARS;
        long remainingDays = daysSinceEpoch + DAYS_TO_SKIP - ((eraIndex - 1) * DAYS_IN_BIG_AGE_CYCLE);

        long cycleIndex = getEraIndexForTwelveEightOrThirteenTwoCycle(daysSinceEpoch);
        long yearsFromCycles = (cycleIndex > TWELVE_EIGHT_CIRCUITS) ? TWELVE_EIGHT_CIRCUITS_TOTAL_YEARS : (cycleIndex - 1) * TWELVE_EIGHT_CYCLE_YEARS;

        if (cycleIndex > TWELVE_EIGHT_CIRCUITS) {
            remainingDays -= TWELVE_EIGHT_CIRCUITS_TOTAL_YEARS * DAYS_IN_TWELVE_EIGHT_CYCLE;
        } else {
            remainingDays -= (cycleIndex - 1) * DAYS_IN_TWELVE_EIGHT_CYCLE;
        }

        long year = yearsFromEras + yearsFromCycles;
        int subCycleType = getSubCycleTypeByDays(daysSinceEpoch);

        if (subCycleType >= 1 && subCycleType <= 3) {
            remainingDays -= DAYS_IN_TWENTY_NINE_CYCLE + ((subCycleType - 1) * DAYS_IN_THIRTY_THREE_CYCLE);
            year += TWENTY_NINE_CYCLE_YEARS + ((subCycleType - 1) * THIRTY_THREE_CYCLE_YEARS);
        } else if (subCycleType == THIRTY_SEVEN_CYCLE_YEARS) {
            remainingDays -= DAYS_IN_TWENTY_NINE_CYCLE + (2 * DAYS_IN_THIRTY_THREE_CYCLE);
            year += TWENTY_NINE_CYCLE_YEARS + (2 * THIRTY_THREE_CYCLE_YEARS);
        }

        int subCycleIndex = getFiveOrFourYearSubCycleIndex(remainingDays);
        remainingDays -= ((long) LEAP_YEAR_PATTERN[subCycleIndex] * DAYS_IN_NORMAL_YEAR) + subCycleIndex;
        long yearsInSubCycle = (remainingDays - 1) / DAYS_IN_NORMAL_YEAR;
        remainingDays -= DAYS_IN_NORMAL_YEAR * yearsInSubCycle;

        if ((yearsInSubCycle == FOUR_CYCLE_YEARS && LEAP_YEAR_PATTERN[subCycleIndex] > 0) || yearsInSubCycle == FIVE_CYCLE_YEARS) {
            remainingDays--;
        }

        year = year + LEAP_YEAR_PATTERN[subCycleIndex] + yearsInSubCycle + 1 - YEARS_TO_SKIP;
        return new long[]{year, remainingDays};
    }

    public static QoopParsiDateTime getPersianDate() {
        return convertGregorianToPersian(LocalDateTime.now());
    }

    public static boolean isPersianLeapYear(long year) {
        int yearInSubCycle = getYearInSubCycle(year);
        return (yearInSubCycle % 4 == 1) && (yearInSubCycle != 1);
    }

    public static int getDaysInPersianMonth(long year, int month) {
        if (month < 1 || month > 12) {
            throw DomainException.of(INVALID_PERSIAN_MONTH_NUMBER);
        }
        if (month <= 6) return 31;
        if (month < 12) return 30;
        return isPersianLeapYear(year) ? 30 : 29;
    }

    public static long getTotalLeapYearsUpTo(long year) {
        long yearsBeforeBigAge = (getEraIndexForBigAgeCycle(year) - 1) * TOTAL_LEAP_YEARS_IN_BIG_AGE_CYCLE;
        long cycleIndex = getEraIndexForTwelveEightOrThirteenTwoCycle(year);

        long yearsBeforeCycle = (cycleIndex > TWELVE_EIGHT_CIRCUITS) ? LEAP_YEARS_IN_TWELVE_EIGHT_CIRCUITS : (cycleIndex - 1) * LEAP_YEARS_IN_TWELVE_EIGHT_CYCLE;

        long yearsPassedSinceCycleStart = isPersianLeapYear(year) ? (getYearPassedSinceCycleStart(year) - 1) : getYearPassedSinceCycleStart(year);
        int subCycleType = getSubCycleType(year);

        if (subCycleType == TWENTY_NINE_CYCLE_YEARS) {
            yearsPassedSinceCycleStart--;
        } else if (subCycleType >= 1 && subCycleType <= 3) {
            yearsPassedSinceCycleStart -= (1 + subCycleType);
        } else {
            yearsPassedSinceCycleStart -= 4;
        }

        yearsPassedSinceCycleStart /= 4;
        return yearsBeforeBigAge + yearsBeforeCycle + yearsPassedSinceCycleStart;
    }

    public static int getDayOfWeekNumber(QoopParsiDateTime persianDate) {
        int firstDayOfYear = getFirstDayOfYear(persianDate.getYear());
        int monthHalf = (persianDate.getMonth() <= 6) ? 0 : 1;
        int firstDayOfMonth = firstDayOfYear + (persianDate.getMonth() - 1) * 3 + (monthHalf * 2);
        return (firstDayOfMonth + (persianDate.getDay() - 1)) % 7;
    }

    public static String getDayOfWeekName(QoopParsiDateTime persianDate) {
        return DAY_OF_WEEK_NAMES[getDayOfWeekNumber(persianDate)];
    }

    private static int getFirstDayOfYear(long year) {
        long totalLeapYears = getTotalLeapYearsUpTo(year);
        totalLeapYears %= 7;
        totalLeapYears += ((year - 1) % 7) + 5;
        totalLeapYears %= 7;
        return (int) totalLeapYears;
    }

    private static long getEraIndexForBigAgeCycle(long persianYear) {
        if (persianYear <= 0) {
            throw DomainException.of(INVALID_PERSIAN_YEAR);
        }
        long adjustedYear = persianYear + YEARS_TO_SKIP;
        return (long) Math.ceil(adjustedYear / (double) BIG_AGE_CYCLE_YEARS);
    }

    private static long getEraIndexForBigAgeCycleByDays(long daysSinceEpoch) {
        return (long) Math.ceil((daysSinceEpoch + DAYS_TO_SKIP) / (double) DAYS_IN_BIG_AGE_CYCLE);
    }

    private static long getStartYearOfBigAgeEra(long persianYear) {
        return ((getEraIndexForBigAgeCycle(persianYear) - 1) * BIG_AGE_CYCLE_YEARS) - YEARS_TO_SKIP;
    }

    private static long getYearPassedSinceBigAgeEraStart(long persianYear) {
        return persianYear - getStartYearOfBigAgeEra(persianYear);
    }

    private static long getEraIndexForTwelveEightOrThirteenTwoCycle(long persianYear) {
        return (long) Math.ceil(getYearPassedSinceBigAgeEraStart(persianYear) / (double) TWELVE_EIGHT_CYCLE_YEARS);
    }

    private static long getEraIndexForTwelveEightOrThirteenTwoCycleByDays(long daysSinceEpoch) {
        long restDays = (daysSinceEpoch + DAYS_TO_SKIP) - ((getEraIndexForBigAgeCycleByDays(daysSinceEpoch) - 1) * DAYS_IN_BIG_AGE_CYCLE);
        restDays /= DAYS_IN_TWELVE_EIGHT_CYCLE;
        return (long) Math.ceil(restDays);
    }

    private static long getStartYearOfTwelveEightOrThirteenTwoCycle(long persianYear) {
        long eraIndex = getEraIndexForTwelveEightOrThirteenTwoCycle(persianYear);
        long yearsBeforeCycle = (eraIndex > TWELVE_EIGHT_CIRCUITS) ? TWELVE_EIGHT_CIRCUITS_TOTAL_YEARS : (eraIndex - 1) * TWELVE_EIGHT_CYCLE_YEARS;

        if (eraIndex > TWELVE_EIGHT_CIRCUITS) {
            return ((getEraIndexForBigAgeCycle(persianYear) * yearsBeforeCycle) + ((getEraIndexForBigAgeCycle(persianYear) - 1) * THIRTEEN_TWO_CYCLE_YEARS)) - YEARS_TO_SKIP;
        } else {
            return getStartYearOfBigAgeEra(persianYear) + yearsBeforeCycle;
        }
    }

    private static long getYearPassedSinceCycleStart(long persianYear) {
        return persianYear - getStartYearOfTwelveEightOrThirteenTwoCycle(persianYear);
    }

    private static long getYearPassedSinceTwelveEightOrThirteenTwoCycleStart(long persianYear) {
        return persianYear - getStartYearOfTwelveEightOrThirteenTwoCycle(persianYear);
    }

    private static long get33YearSubCycleIndex(long yearsPassedSinceCycleStart) {
        return (long) Math.ceil((yearsPassedSinceCycleStart - TWENTY_NINE_CYCLE_YEARS) / (double) THIRTY_THREE_CYCLE_YEARS);
    }

    private static long get33YearSubCycleIndexByDays(long daysSinceEpoch) {
        return (long) Math.ceil((daysSinceEpoch - DAYS_IN_TWENTY_NINE_CYCLE) / (double) DAYS_IN_THIRTY_THREE_CYCLE);
    }

    private static long getYearsPassedIn33YearSubCycle(long yearsPassedSinceCycleStart) {
        return TWENTY_NINE_CYCLE_YEARS + ((get33YearSubCycleIndex(yearsPassedSinceCycleStart) - 1) * THIRTY_THREE_CYCLE_YEARS);
    }

    private static int getYearInSubCycle(long persianYear) {
        long eraIndex = getEraIndexForTwelveEightOrThirteenTwoCycle(persianYear);
        long yearsPassed = getYearPassedSinceTwelveEightOrThirteenTwoCycleStart(persianYear);

        if (eraIndex > TWELVE_EIGHT_CIRCUITS) {
            if (yearsPassed > TWENTY_NINE_CYCLE_YEARS && yearsPassed <= YEARS_BEFORE_THIRTY_SEVEN_CYCLE) {
                return (int) (yearsPassed - getYearsPassedIn33YearSubCycle(yearsPassed));
            } else if (yearsPassed > YEARS_BEFORE_THIRTY_SEVEN_CYCLE) {
                return (int) (yearsPassed - YEARS_BEFORE_THIRTY_SEVEN_CYCLE);
            } else {
                return (int) yearsPassed;
            }
        } else {
            if (yearsPassed > TWENTY_NINE_CYCLE_YEARS) {
                return (int) (yearsPassed - getYearsPassedIn33YearSubCycle(yearsPassed));
            } else {
                return (int) yearsPassed;
            }
        }
    }

    private static int getSubCycleType(long persianYear) {
        long eraIndex = getEraIndexForTwelveEightOrThirteenTwoCycle(persianYear);
        long yearsPassed = getYearPassedSinceTwelveEightOrThirteenTwoCycleStart(persianYear);

        if (eraIndex > TWELVE_EIGHT_CIRCUITS) {
            if (yearsPassed > TWENTY_NINE_CYCLE_YEARS && yearsPassed <= YEARS_BEFORE_THIRTY_SEVEN_CYCLE) {
                return (int) get33YearSubCycleIndex(yearsPassed);
            } else if (yearsPassed > YEARS_BEFORE_THIRTY_SEVEN_CYCLE) {
                return (int) THIRTY_SEVEN_CYCLE_YEARS;
            } else {
                return (int) TWENTY_NINE_CYCLE_YEARS;
            }
        } else {
            if (yearsPassed > TWENTY_NINE_CYCLE_YEARS) {
                return (int) get33YearSubCycleIndex(yearsPassed);
            } else {
                return (int) TWENTY_NINE_CYCLE_YEARS;
            }
        }
    }

    private static int getSubCycleTypeByDays(long daysSinceEpoch) {
        long eraIndex = getEraIndexForTwelveEightOrThirteenTwoCycleByDays(daysSinceEpoch);
        long restDays = (daysSinceEpoch + DAYS_TO_SKIP) - ((getEraIndexForBigAgeCycleByDays(daysSinceEpoch) - 1) * DAYS_IN_BIG_AGE_CYCLE);

        if (eraIndex > TWELVE_EIGHT_CIRCUITS) {
            restDays -= TWELVE_EIGHT_CIRCUITS_TOTAL_YEARS * DAYS_IN_TWELVE_EIGHT_CYCLE;
        } else {
            restDays -= (eraIndex - 1) * DAYS_IN_TWELVE_EIGHT_CYCLE;
        }

        if (eraIndex > TWELVE_EIGHT_CIRCUITS) {
            if (restDays > DAYS_IN_TWENTY_NINE_CYCLE && restDays <= DAYS_IN_TWENTY_NINE_CYCLE + (2 * DAYS_IN_THIRTY_THREE_CYCLE)) {
                return (int) get33YearSubCycleIndexByDays(restDays);
            } else if (restDays > DAYS_IN_TWENTY_NINE_CYCLE + (2 * DAYS_IN_THIRTY_THREE_CYCLE)) {
                return THIRTY_SEVEN_CYCLE_YEARS;
            } else {
                return TWENTY_NINE_CYCLE_YEARS;
            }
        } else {
            if (restDays > DAYS_IN_TWENTY_NINE_CYCLE) {
                return (int) get33YearSubCycleIndexByDays(restDays);
            } else {
                return (int) TWENTY_NINE_CYCLE_YEARS;
            }
        }
    }

    private static int getFiveOrFourYearSubCycleIndex(long daysSinceEpoch) {
        int index = 0;
        if (daysSinceEpoch > DAYS_IN_FIVE_CYCLE) {
            daysSinceEpoch -= DAYS_IN_FIVE_CYCLE;
            index = (int) Math.ceil(daysSinceEpoch / (double) DAYS_IN_FOUR_CYCLE);
        }
        return index;
    }
}