//package io.qoop.date.jalali;
//
//import java.time.Duration;
//
//import static io.qoop.date.jalali.JalaliConverter.isLeapYear;
//import static io.qoop.date.jalali.JalaliExceptionCode.*;
//
///**
// * @author Davood Akbari - 1404
// * daak1365@gmail.com
// * daak1365@yahoo.com
// * 09125188694
// */
//
//public class JalaliDateTime {
//
//    private long year;
//    private int month;
//    private int day;
//    private int hour;
//    private int minute;
//    private int second;
//    private int microsecond;
//
//    public JalaliDateTime(long year, int month, int dayOfMonth, int hour, int minute, int second, int microsecond) {
//        /*
//         * Constructor
//         * متدی که داده را با توجه به پارامتر ورودی می سازد
//         * "year"=>سال خورشیدی بین - 1 و 9223372036854775807 است
//         * "month"=>برج خورشیدی - بین 1 و 12 است
//         * "dayOfMonth"=>روز خورشیدی - بین 1 و حداکثر تعداد روز ماه مورد نظر
//         * "hour"=>ساعت - بین 0 و 23 است
//         * "minute"=>دقیقه - بین 0 و 59 است
//         * "second"=>ثانیه - بین 0 و 59 است
//         * "microsecond"=>میلی ثانیه - بین 0 و 999999 است
//         * "توضیح"=> سال کمتر از 1 و بیشتر از9223372036854775807 یا برج کمتر از 1 و بیشتر از 12 یا روز کمتر از 1 و بیشتر از تعداد روزش در هر سال یا ساعت کمتر از 0 و بیشتز از 23 یا دقیقه کمتر از 0 و بیشتر از 59 یا ثانیه کمتر از 0 و بیشتر از 59 و میلی ثانیه کمتر از صفر و بیشتر از 999
//         */
//
//        JalaliMonth jalaliMonth = JalaliMonth.of(month);
//        short length = jalaliMonth.length(isLeapYear(year));
//
//        if (year <= 0) {
//            throw JalaliDateTimeException.of(INVALID_PERSIAN_YEAR);
//        } else if ((month > 12) || (month < 1)) {
//            throw JalaliDateTimeException.of(INVALID_PERSIAN_MONTH_NUMBER);
//        } else if ((dayOfMonth < 1) || (dayOfMonth > length)) {
//            throw JalaliDateTimeException.of(INVALID_PERSIAN_DAY_RANGE, length);
//        } else if ((hour < 0) || (hour > 23)) {
//            throw new DAexception("ساعت بین 0 و 23 است");
//        } else if ((minute < 0) || (minute > 59)) {
//            throw new DAexception("دقیقه بین 0 و 59 است");
//        } else if ((second < 0) || (second > 59)) {
//            throw new DAexception("ثانیه بین 0 و 59 است");
//        } else if ((microsecond < 0) || (microsecond > 999999)) {
//            throw new DAexception("میلی ثانیه بین 0 و 999999 است");
//        }
//
//        this.year = year;
//        this.month = month;
//        this.day = dayOfMonth;
//        this.hour = hour;
//        this.minute = minute;
//        this.second = second;
//        this.microsecond = microsecond;
//    }
//
//    // Constructor پیش‌فرض برای مقادیر اولیه
//    public JalaliDateTime() {
//        this(1, 1, 1, 0, 0, 0, 0);
//    }
//
//    private String __addZero(String str) {
//        if (str.length() < 2) {
//            str = '0' + str;
//        }
//        return str;
//    }
//
//    // رشته تاریخ
//    @Override
//    public String toString() {
//        return String.valueOf((int) this.year) + '-' + this.__addZero(String.valueOf((int) this.month)) + '-' + this.__addZero(String.valueOf((int) this.day)) + ' ' + this.__addZero(String.valueOf((int) this.hour)) + ':' + this.__addZero(String.valueOf(this.minute)) + ':' + this.__addZero(String.valueOf(this.second)) + '.' + this.__addZero(String.valueOf(this.microsecond));
//    }
//
//    // رشته ی تاریخ پارسی
//    public String parsiStrDateTime() {
//        return String.valueOf((int) this.year) + '/' + this.__addZero(String.valueOf((int) this.month)) + '/' + this.__addZero(String.valueOf((int) this.day)) + ' ' + this.__addZero(String.valueOf((int) this.hour)) + ':' + this.__addZero(String.valueOf(this.minute)) + ':' + this.__addZero(String.valueOf(this.second)) + '.' + this.__addZero(String.valueOf(this.microsecond));
//    }
//
//    // حداکثر تاریخ خورشیدی
//    public JalaliDateTime getMaxValue() {
//        return new JalaliDateTime(9223372036854775807L, 12, 29, 23, 59, 59, 999999);
//    }
//
//
//    // حداقل تاریخ خورشیدی
//    public JalaliDateTime getMinValue() {
//        return new JalaliDateTime();
//    }
//
//    // شماره روز هفته تاریخ مورد نظر
//    public int getDayOfWeekNumber() {
//        return JalaliConverter.dayOfWeekNumber(this);
//    }
//
//
//    // میلی ثانیه
//    public int getMicrosecond() {
//        return this.microsecond;
//    }
//
//
//    // ثانیه
//    public int getSecond() {
//        return this.second;
//    }
//
//
//    // دقیقه
//    public int getMinute() {
//        return this.minute;
//    }
//
//    // ساعت
//    public int getHour() {
//        return this.hour;
//    }
//
//    // روز
//    public int getDay() {
//        return this.day;
//    }
//
//    // برج
//    public int getMonth() {
//        return this.month;
//    }
//
//    // سال
//    public long getYear() {
//        return this.year;
//    }
//
//    // تاریخ و زمان حالا
//    public JalaliDateTime getNow() {
//        return JalaliConverter.now();
//    }
//
//    public JalaliDateTime addYears(long Years) {
//        /*
//         * اضافه کردن به سال
//         * "Years"=>تعداد سال
//         */
//        return new JalaliDateTime(this.getYear() + Years, this.getMonth(), this.getDay(), this.getHour(), this.getMinute(), this.getSecond(), this.getMicrosecond());
//    }
//
//    /*
//     * این تابع مشکلی ندارد ولی تابع بهینه شده در کد های زبان پی اچ پی موجود است
//     */
//    public JalaliDateTime addMonths(int months) {
//        /*
//         * اضافه کردن به برج
//         * "months"=>تعداد برج
//         */
//        if (months != 0) {
//            // در زیر تعداد سال یا ماهی که قرار است کم یا زیاد شود بدست می آید
//            long tempYear = Math.abs(months) / 12;
//            int tempMonth = Math.abs(months) % 12;
//
//            // در زیر اگر ورودی صفر باشد هیچ عملی انجام نمی شود ولی اگر منفی یا مثبت باشد با توجه به آن ماه و سال بدست می آید
//            if (months > 0) {
//                tempMonth = this.getMonth() + tempMonth;
//                if (tempMonth > 12) {
//                    tempYear += (tempMonth / 12); // حداکثر یکسال اضافه می شود
//                    tempMonth = (tempMonth % 12);
//                }
//            } else {
//                tempMonth = this.getMonth() - tempMonth;
//                if (tempMonth <= 0) {
//                    tempMonth = 12 + tempMonth; // با این دلیل با 12 جمع می شود زیرا عدد منقی است و خروجی عددی مثبت و ماه مورد نظر است
//                    tempYear += 1; // در زیر .سال با تابع مربوط به خود کم یا زیاد می شود و جمع با ۱ یعنی یکسال دیگر برای کم شدن اضافه می شود
//                }
//                tempYear = -tempYear; // در اینجا منفی می شود زیرا می خواهیم اگر با تابع حساب شد کم کند
//            }
//
//            return new JalaliDateTime(this.addYears(tempYear).getYear(), tempMonth, this.getDay(), this.getHour(), this.getMinute(), this.getSecond(), this.getMicrosecond());
//        }
//
//        return this;
//    }
//
//    public JalaliDateTime addDays(long days) {
//        /*
//         * اضافه کردن به روز
//         * "days"=>تعداد روز
//         */
//        // فرض بر این است که DAConvertDate.convertToGregorian یک شیء java.timeInstant یا مشابه برمی‌گرداند
//        // و DAConvertDate.convertToParsiDate آن را دوباره به DaParsiDateTime تبدیل می‌کند.
//        // در جاوا برای اضافه کردن زمان از Duration استفاده می‌کنیم.
//        var gregorian = JalaliConverter.toGregorian(this);
//        var updatedGregorian = gregorian.plus(Duration.ofDays(days));
//        return JalaliConverter.toJalali(updatedGregorian);
//    }
//
//    public JalaliDateTime addHours(long hours) {
//        /*
//         * اضافه کردن به ساعت
//         * "hours"=>تعداد ساعت
//         */
//        var gregorian = JalaliConverter.toGregorian(this);
//        var updatedGregorian = gregorian.plus(Duration.ofHours(hours));
//        return JalaliConverter.toJalali(updatedGregorian);
//    }
//
//    public JalaliDateTime addMinutes(long minutes) {
//        /*
//         * اضافه کردن به دقیقه
//         * "minutes"=>تعداد دقیقه
//         */
//        var gregorian = JalaliConverter.toGregorian(this);
//        var updatedGregorian = gregorian.plus(Duration.ofMinutes(minutes));
//        return JalaliConverter.toJalali(updatedGregorian);
//    }
//
//    public JalaliDateTime addSeconds(long seconds) {
//        /*
//         * اضافه کردن به ثانیه
//         * "seconds"=>تعداد ثانیه
//         */
//        var gregorian = JalaliConverter.toGregorian(this);
//        var updatedGregorian = gregorian.plus(Duration.ofSeconds(seconds));
//        return JalaliConverter.toJalali(updatedGregorian);
//    }
//
//    public JalaliDateTime addMicroseconds(long microseconds) {
//        /*
//         * اضافه کردن به میکرو ثانیه
//         * "microseconds"=>تعداد میکرو ثانیه
//         */
//        var gregorian = JalaliConverter.toGregorian(this);
//        var updatedGregorian = gregorian.plus(Duration.ofNanos(microseconds * 1000));
//        return JalaliConverter.toJalali(updatedGregorian);
//    }
//}