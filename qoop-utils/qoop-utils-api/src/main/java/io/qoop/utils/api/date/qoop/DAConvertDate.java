package io.qoop.utils.api.date.qoop;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

// #coding: utf-8
/*
  تبدیل تاریخ میلادی به خورشید -
  برنامه نویس : داود اکبری -
  1390-1385
 
  .تاریخ هجری خورشیدی در حالت کلی به دوره های بزرگ 2820 ساله تقسیم شده است 
  .که خود این دروره بزرگ به 21 دوره 128 ساله و یک دوره 132 ساله تقسیم شده است
  .دوره 128 ساله شامل یک دوره 29 ساله و 3 دوره 33 ساله 
  .و دروه 132 ساله شامل یک دوره 29 ساله و دو دوره  33 ساله و در آخر یک دوره 37 ساله است
  .دوره 29 ساله دارای یک دوره 5 ساله و 6 دوره 4 ساله است
  .دوره 33 ساله دارای یک دوره 5 ساله و 7 دوره 4 ساله است
  .دوره 37 ساله دارای یک دوره 5 ساله و 8 دوره 4 ساله است
  .سال های آخر دوره های 5  یا 4 ساله کبیسه است
  .ساله های کبیسه 366 روز و سال های عادی 365 روز دارند
  .منظور از عدد تنها 128 یا 132 یا 29 و... دوره های مربوطه است
*/

// import DAClassLibrary import DaParsiDateTime, DAexception
// import math
// import datetime
// import macpath import split

public class DAConvertDate {

    // روز اول سال خورشیدی
    private static final LocalDateTime firstDay = LocalDateTime.of(622, 3, 22, 0, 0, 0);
    // سال های در نظر گرفته نشده - آغاز سال هجری خورشیدی
    private static final long yearsCountblink = 2346;
    // تعداد سال دوره بزرگ
    private static final long yearsCountBigAGE = 2820;
    // تعداد سال دوره 128ساله
    private static final long yearsCount128 = 128;
    // تعداد سال دوره 132 ساله
    private static final long yearsCount132 = 132;
    // تعداد سال دروه 29 ساله
    private static final long yearsCount29 = 29;
    // تعداد سال دروه 33 ساله
    private static final long yearsCount33 = 33;
    // تعداد سال دروه 37 ساله
    private static final long yearsCount37 = 37;
    // تعداد سال دروه 5 ساله
    private static final long yearsCount5 = 5;
    // تعداد سال دروه 4 ساله
    private static final long yearsCount4 = 4;
    // تعداد دروه 128 ساله
    private static final long Count128Circuit = 21;
    // تعداد دروه 132 ساله
    private static final long Count132Circuit = 1;
    // تعداد سال دوره های 128 ساله
    private static final long yearsCount128AGES = Count128Circuit * yearsCount128;
    // تعداد سال دوره های 132 ساله
    private static final long yearsCount132AGES = Count132Circuit * yearsCount132;
    // تعداد سال گذشته شده تا قبل از زیر دوره 37 ساله
    private static final long yearsCountBefore37 = yearsCount29 + (2 * yearsCount33);

    // تعداد سال کبیسه دوره 128 ساله
    private static final long anomalyYearsCount128 = 31;
    // تعداد سال کبیسه دوره 132 ساله
    private static final long anomalyYearsCount132 = 32;
    // تعداد سال کبیسه دوره 29 ساله
    private static final long anomalyYearsCount29 = 7;
    // تعداد سال کبیسه دوره 33 ساله
    private static final long anomalyYearsCount33 = 8;
    // تعداد سال کبیسه دوره 37 ساله
    private static final long anomalyYearsCount37 = 9;
    // تعداد سال کبیسه دوره کوچک 4 یا 5 ساله
    private static final long anomalyYearsCount4OR5 = 1;
    // تعداد سال کبیسه کل دوره های 128 ساله
    private static final long anomalyYearsCount128AGES = Count128Circuit * anomalyYearsCount128;
    // تعداد سال کبیسه کل دوره های 132 ساله
    private static final long anomalyYearsCount132AGES = Count132Circuit * anomalyYearsCount132;
    // تعداد سال کبیسه دوره بزرگ
    private static final long anomalyYearsCountBigAGE = anomalyYearsCount128AGES + anomalyYearsCount132AGES;
    // تعداد سال کبیسه سال هایی که در نظر گرفته نشده است
    // به این دلیل با 3 و 7 جمع شده که 42 سال دارای یک دوره 29 ساله است که دارای 7 سال کبیسه و 13 سال باقی در دوره 33 ساله است که 13 سال داری 3 سال کبیسه است 
    private static final long anomalyYearsCountBlink = ((yearsCountblink / yearsCount128) * anomalyYearsCount128) + anomalyYearsCount29 + 3;
    // تعداد روز سال عادی
    private static final long daysCountNormalYear = 365;
    // تعداد روز سال کبیسه
    private static final long daysCountAnomalyYear = 366;
    // تعداد روز سال هایی که در نظر گرفته نشده
    private static final long daysCountBlinkYear = (yearsCountblink * daysCountNormalYear) + anomalyYearsCountBlink;
    // تعداد روز دوره بزرگ 2820 ساله
    private static final long daysCountBigAGE = (yearsCountBigAGE * daysCountNormalYear) + anomalyYearsCountBigAGE;
    // تعداد روز دوره 128 ساله
    private static final long daysCount128 = (yearsCount128 * daysCountNormalYear) + anomalyYearsCount128;
    // تعداد روز دوره 132 ساله
    private static final long daysCount132 = (yearsCount132 * daysCountNormalYear) + anomalyYearsCount132;
    // تعداد روز دوره 29 ساله
    private static final long daysCount29 = (yearsCount29 * daysCountNormalYear) + anomalyYearsCount29;
    // تعداد روز دوره 33 ساله
    private static final long daysCount33 = (yearsCount33 * daysCountNormalYear) + anomalyYearsCount33;
    // تعداد روز دوره 37 ساله
    private static final long daysCount37 = (yearsCount37 * daysCountNormalYear) + anomalyYearsCount37;
    // تعداد روز دوره کوچک 5 ساله
    private static final long daysCount5 = (yearsCount5 * daysCountNormalYear) + anomalyYearsCount4OR5;
    // تعداد روز دوره کوچک 4 ساله
    private static final long daysCount4 = (yearsCount4 * daysCountNormalYear) + anomalyYearsCount4OR5;

    // شماره سال های کبیسه
    private static final long[] arrYearNumber = {0, 5, 9, 13, 17, 21, 25, 29, 33, 37};
    // نام برج های سال
    public static final String[] parsiMonthName = {
            "", // Index 0 placeholder
            "فروردین",
            "ارديبهشت",
            "خرداد",
            "تير",
            "امرداد",
            "شهريور",
            "مهر",
            "آبان",
            "آذر",
            "دى",
            "بهمن",
            "اسفند"
    };
    // روزهای هفته
    private static final List<String> parsiDayOfWeek = Arrays.asList(
            "شنبه",
            "يكشنبه",
            "دوشنبه",
            "سه شنبه",
            "چهارشنبه",
            "پنجشنبه",
            "جمعه"
    );

    // تعداد روز ماه های نیمه اول سال
    private static final long daysCountYearFirstMid = 31;
    // تعداد روز ماه های نیمه دوم سال
    private static final long daysCountYearSecMid = 30;
    // تعداد ماه در هر نیمه سال
    private static final long monthCountEachYearMid = 6;
    // تعداد ماه هر سال
    private static final long monthCountEachYear = monthCountEachYearMid * 2;
    // تعداد روز هفته
    private static final long weekDaysCount = 7;

    // --------
    public static long which2820(long ParsiYear) {
        /*
          بدست آوردن اینکه سال مورد نظر در کدام دوره بزرگ است
         "ParsiYear"=>سال خورشیدی
         خروجی نشان می دهد که سال مورد نظر در کدام دوره بزرگ است
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        if (ParsiYear <= 0) {
            throw new DAexception(".سال مورد قبول از یک شروع می شود");
        }
        ParsiYear += yearsCountblink;
        return (long) Math.ceil(ParsiYear / (double) yearsCountBigAGE); // ما سقف عدد بدست آمده را می خواهیم زیرا رقم سمت راست به این معنی است که وارد دوره شده است
    }

    // ---------    
    public static long which2820AtDay(long LastDays) {
        /*
         بدست آوردن اینکه تعداد روز ورودی در  کدام دوره بزرگ است
         "LastDays"=>تعداد روز گذشته شده از ابتدای تاریخ خورشیدی
         خروجی نشان دهنده کدام دوره بزرگ است
        */
        return (long) Math.ceil((LastDays + daysCountBlinkYear) / (double) daysCountBigAGE);
    }

    // ----------
    public static long eraBigAGE(long ParsiYear) {
        /*
         بدست آوردن مبدا دروه ی بزرگ سالی که از ورودی گرفته می شود
         "ParsiYear"=>سال خورشیدی
         مبدا دوره بزرگ سال مورد نظر
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        return ((which2820(ParsiYear) - 1) * yearsCountBigAGE) - yearsCountblink;
    }

    // -----------

    public static long passedFromEraBigAGE(long ParsiYear) {
        /*
         سال طی شده از مبدا دوره مورد نظر بزرگ
         "ParsiYear"=>سال خورشیدی
         سال طی شده از مبدا دوره مورد نظر بزرگ
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        return ParsiYear - eraBigAGE(ParsiYear);
    }

    // -----------

    public static long which128Or132(long ParsiYear) {
        /*
         سال مورد نظر در کدام دوره ی 128 یا 132 است
         "ParsiYear"=>سال خورشیدی
         سال مورد نظر در کدام دوره ی 128 یا 132 است
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        // چون دور بزرگ به 21 دوره 128 و یک دوره 132 تقسیم شده
        return (long) Math.ceil(passedFromEraBigAGE(ParsiYear) / (double) yearsCount128);
    }

    // ------------------   

    public static long which128Or132AtDay(long LastDays) {
        /*
         سال مورد نظر در کدام دوره ی128 یا 132 قرار دارد 
         "LastDays"=>ورودی برحسب تعداد روز گذشته شده از ابتدای تاریخ خورشیدی
         سال مورد نظر در کدام دوره ی 128 یا 132  قرار دارد  
        */
        double RestDays = (LastDays + daysCountBlinkYear) - ((which2820AtDay(LastDays) - 1) * daysCountBigAGE);
        RestDays /= (double) daysCount128;
        return (long) Math.ceil(RestDays);
    }

    // --------------

    public static long era128Or132(long ParsiYear) {
        /*
         بدست آوردن مبدا دوره 128 یا 132 ای که سال مورد نظر در آن قرار دارد
         "ParsiYear"=>سال خورشیدی
         بدست آوردن مبدا دوره 128 یا 132 ای که سال مورد نظر در آن قرار دارد  
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود

        */
        long What = which128Or132(ParsiYear);
        long LateYear = 0;
        if (What > Count128Circuit) {
            LateYear = yearsCount128AGES;
        } else {
            LateYear = ((What - 1) * yearsCount128);
        }

        if (What > Count128Circuit) {
            return (((which2820(ParsiYear) * LateYear) + ((which2820(ParsiYear) - 1) * yearsCount132)) - yearsCountblink);
        } else {
            return (eraBigAGE(ParsiYear) + LateYear);
        }
    }

    // ---------------    

    public static long passedFromEra128Or132(long ParsiYear) {
        /*
         چند سال از مبدا دوره 128 یا 132 که سال مورد نظر در آن قرار دارد گذشته است
         "ParsiYear"=>سال خورشیدی
         چند سال از مبدا دوره 128 یا 132 که سال مورد نظر در آن قرار دارد گذشته است 
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود

        */
        return (ParsiYear - era128Or132(ParsiYear));
    }

    // --------------------

    public static long whichAge33(long LastParsiYear) {
        /*
         چندمین دوره 33 ساله
         "LastParsiYear"=>تعداد سال گدشته شده از مبدا دوره 128 یا 132 ساله    
        */
        return (long) Math.ceil((LastParsiYear - yearsCount29) / (double) yearsCount33);
    }

    // ----------------

    public static long whichAge33FromRestDays(double RestDays) {
        /*
          "RestDays"=>تعداد روز گدشته شده از مبدا دوره 128 یا 132 ساله
         */
        return (long) Math.ceil((RestDays - daysCount29) / (double) daysCount33);
    }

    // ------------------------------ 

    public static long passedAge33(long LastParsiYear) {
        /*
         چند سال از ابتدای دوره 33 ساله می گذرد
         "LastParsiYear"=>تعداد سال گدشته شده از مبدا دوره 128 یا 132 ساله
        */
        return (yearsCount29 + ((whichAge33(LastParsiYear) - 1) * yearsCount33));
    }

    // -----------------------

    public static long whatYearOFSubAge(long ParsiYear) {
        /*
         سال چندم در هر زیر دوره 29 یا 33 یا 37 است
         "ParsiYear"=>سال خورشیدی

         سال چندم در هر زیر دوره 29 یا 33 یا 37 است 
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        long What = which128Or132(ParsiYear);
        long Last = passedFromEra128Or132(ParsiYear);
        if (What > Count128Circuit) {
            if ((Last <= yearsCountBefore37) && (Last > yearsCount29)) {
                return (Last - passedAge33(Last));
            } else {
                if (Last > yearsCountBefore37) {
                    return (Last - yearsCountBefore37);
                } else {
                    return Last;
                }
            }
        } else {
            if (Last > yearsCount29) {
                return Last - passedAge33(Last);
            } else {
                return Last;
            }
        }
    }

    // -----------------------  
    public static long whichSubAge29Or33Or37(long ParsiYear) {
        /*
         در کدام زیر دوره 29 یا 33 یا 37 است
         "ParsiYear"=>سال خورشیدی
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود   
        */
        long What = which128Or132(ParsiYear);
        long Last = passedFromEra128Or132(ParsiYear);

        if (What > Count128Circuit) {
            if ((Last <= yearsCountBefore37) && (Last > yearsCount29)) {
                return whichAge33(Last);
            } else {
                if (Last > yearsCountBefore37) {
                    return yearsCount37;
                } else {
                    return yearsCount29;
                }
            }
        } else {
            if (Last > yearsCount29) {
                return whichAge33(Last);
            } else {
                return yearsCount29;
            }
        }
    }

    // -------------------------  

    public static long whichSubAge29Or33Or37AtDay(long LastDays) {
        /*
         در کدام زیر دوره 29 یا 33 یا 37 است
         "LastDays"=>روز گذشته شده از ابتدای دوره 128 یا 132   
        */

        long What128Or132 = which128Or132AtDay(LastDays);
        double RestDays = (LastDays + daysCountBlinkYear) - ((which2820AtDay(LastDays) - 1) * daysCountBigAGE);
        if (What128Or132 > Count128Circuit) {
            RestDays -= (Count128Circuit * daysCount128);
        } else {
            RestDays -= ((What128Or132 - 1) * daysCount128);
        }

        if (What128Or132 > Count128Circuit) {
            if ((RestDays > daysCount29) && (RestDays <= (daysCount29 + (2 * daysCount33)))) {
                return whichAge33FromRestDays(RestDays);
            } else {
                if (RestDays > (daysCount29 + (2 * daysCount33))) {
                    return yearsCount37;
                } else {
                    return yearsCount29;
                }
            }
        } else {
            if (RestDays > daysCount29) {
                return whichAge33FromRestDays(RestDays);
            } else {
                return yearsCount29;
            }
        }
    }

    // ------------------------------------ 

    public static boolean leapYear(long ParsiYear) {
        /*
           شناسایی اینکه سال کبیسه است یا نه
           "ParsiYear"=>سال خورشیدی
           اینکه سال کبیسه است یا نه 
           توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        long Year = whatYearOFSubAge(ParsiYear);
        return ((Year % yearsCount4 == 1) && (Year != 1));
    }

    // ---------------

    public static int parsiMonthDaysCount(long ParsiYear, int Month) {
        /*
         تعداد روز های برج های ایرانی
         "ParsiYear"=>سال خورشیدی
         "Month"=>برج خورشیدی
         اینکه سال کبیسه است یا نه 
         توضیح=>اگر شماره ماه از 1 کوچکتر و از 12 بزرگتر باشد خطا صادر می شود -یا- اگر سال از 1 کمتر باشد     
        */

        if ((Month > 12) || (Month < 1)) {
            throw new DAexception(".شماره برح های فارسی از 1 شروع شده و به 12 ختم می شود");
        }

        if ((Month >= 1) && (Month <= 6)) {
            return 31;
        } else {
            if (((Month >= 7) && (Month <= 11))) {
                return 30;
            } else {
                if ((Month == 12) && (leapYear(ParsiYear))) {
                    return 30;
                } else {
                    return 29;
                }
            }
        }
    }

    // ---------------------------

    public static long multipleLeapYear(long ParsiYear) {
        /*
         تعداد سال کبسه تا قبل از سال مورد نظر
         "ParsiYear"=>سال خورشیدی

         تعداد سال کبسه تا قبل از سال مورد نظر
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */
        // دوره 128 ساله ,31 سال و در دوره ی 132 ساله, 32 سال کبيسه وجود دارد و در هر دوره بزرگ 21 دوره 128 ساله و 1 دوره 132 ساله داريم.
        long Befor2820Now = (which2820(ParsiYear) - 1) * anomalyYearsCountBigAGE; // تعداد سال های کبيسه دوره های 2820 بزرگ قبل از دوره بزرگ سال مورد نظر
        long What = which128Or132(ParsiYear);

        long Befor128Or132Now;
        if (What > Count128Circuit) {
            Befor128Or132Now = anomalyYearsCount128AGES;
        } else {
            Befor128Or132Now = (What - 1) * anomalyYearsCount128;
        }
        // تعداد سال کبیسه تا قبل از دور 132 سال مورد نظر موجود- بعد از ؟
        // تعداد سال کبیسه تا قبل از دور 128 سال مورد نظر موجود - بعد از دونقطه

        // تعداد سال کبیسه طی شده تا قبل از سال مورد نظر در داخل دوره 128 یا 132
        // تعداد سال دقیق طی شده

        long LastFrom128Or132;
        if (leapYear(ParsiYear)) { // سال طی شده از ابتدای دوره 128 یا 132 تا سال مورد نظر
            LastFrom128Or132 = (passedFromEra128Or132(ParsiYear) - 1);
        } else {
            LastFrom128Or132 = passedFromEra128Or132(ParsiYear);
        }

        long SubAge = whichSubAge29Or33Or37(ParsiYear);

        if (SubAge == yearsCount29) {
            LastFrom128Or132 -= 1;
        } else {
            if ((SubAge >= 1) && (SubAge <= 3)) {
                LastFrom128Or132 -= (1 + SubAge);
            } else {
                LastFrom128Or132 -= 4;
            }
        }

        LastFrom128Or132 /= 4; // /تعداد سال کبیسه طی شده از ابتدای 128 یا 132 تا سال مورد نظر

        return Befor2820Now + Befor128Or132Now + LastFrom128Or132; // تعداد سال کبیسه کل
    }

    // --------------------------

    public static JalaliDateTime dayToDate(long ParsiYear, long RestDay) {
        /*
         تبدیل روز به تاریخ خورشیدی با گرفتن سال و تعداد روز باقی مانده
         "ParsiYear"=>سال فارسی
         "RestDay"=>روز باقی مانده

         تاریخ روز مورد نظر 
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطا صادر می شود
        */

        long daysCountFirstMid = monthCountEachYearMid * daysCountYearFirstMid; // تعداد روز نیمه اول سال
        RestDay += 1;
        boolean IsAYearEndDay = ((RestDay / daysCountAnomalyYear) == 1) && (!leapYear(ParsiYear)); // فهمیدن اینکه روز آخر هست و سال کبیسه نیست
        // به منظور ننوشتن دستورات شرطی تو در تو 30 یا 31 را به این صورت می شناسیم
        long MonthDaysCount = (long) (daysCountYearSecMid + (2 - Math.ceil(RestDay / (double) daysCountFirstMid)));
        long WhatIsAMid = (long) (RestDay / daysCountFirstMid); // صفر مشخص کننده نیمه اول و یک مشخص کننده نیمه دوم است
        // به این دلیل مقدار را از 2 کم می کنیم جون دو نیم سال دارم و سال دوم باید صفر باشد
        RestDay -= (daysCountFirstMid * WhatIsAMid);
        double Month = RestDay / (double) MonthDaysCount;
        long IsAEndDay = (long) ((RestDay - ((Math.ceil(Month) - 1) * MonthDaysCount)) / MonthDaysCount); // اگز روز آخر ماه باشد 1 وگرنه صفر است
        Month = Math.ceil(Month) + (monthCountEachYearMid * WhatIsAMid); // اگر در ماه دوم باشیم با 6 چمع می شود
        RestDay = (long) ((RestDay % MonthDaysCount) + (IsAEndDay * MonthDaysCount));

        if (IsAYearEndDay) {
            RestDay -= (daysCountYearSecMid - 1);
        }

        if (IsAYearEndDay) {
            Month -= (monthCountEachYear - 1);
        }

        if (IsAYearEndDay) {
            ParsiYear += 1;
        }

        return new JalaliDateTime(ParsiYear, (int) Month, (int) RestDay, 0, 0, 0, 0);
    }

    // --------------------------

    public static long firstDayYear(long ParsiYear) {
        /*
         محاسبه روز اول سال مورد نظر
         "ParsiYear"=>سال خورشیدی

         محاسبه روز اول سال مورد نظر 
         توضیح=>اگر سال ورودی از 1 کمتر باشد خطاParsiMonthDaysCount صادر می شود   
        */
        long Days = multipleLeapYear(ParsiYear);
        Days %= weekDaysCount;
        Days += (((ParsiYear - 1) % weekDaysCount) + 5); // روز اول سال یک  پنج شنبه بوده و به همین دلیل با 5 جمع می کنیم
        Days %= weekDaysCount;
        return Days;
    }

    // --------------------

    public static long firstDayMonth(long ParsiYear, int MonthNumber) {
        /*
         شماره روز اول برج مورد نظر
         "ParsiYear"=>سال خورشیدی
         "MonthNumber"=>شماه برج
         شماره روز اول برج مورد نظر 
         توضیح=>اگر شماره ماه از 1 کوچکتر و از 12 بزرگتر باشد خطا صادر می شود
        */
        if ((MonthNumber > 12) || (MonthNumber < 1)) {
            throw new DAexception(".شماره برح های فارسی از 1 شروع شده و به 12 ختم می شود");
        }
        long First = firstDayYear(ParsiYear); // روز اول سال مورد نظر

        if (MonthNumber <= weekDaysCount) {
            First += ((MonthNumber - 1) * 3);
        } else {
            First += ((MonthNumber + 2) * 2);
        }

        First %= weekDaysCount;
        return First;
    }

    // ----------------------

    public static int dayOfWeekNumber(JalaliDateTime parsiDate) {
        /*
         شماره روز هفته تاریخ مورد نظر
         "parsiDate"=>تاریخ خورشیدی

         شماره روز هفته تاریخ مورد نظر 
        */
        // روز تاریخ را به این خاطر از یک کم می کنیم تا تعداد روز گذشته شده تا قبل از آن روز را بدست آوریم
        return (int) ((firstDayMonth(parsiDate.getYear(), parsiDate.getMonth()) + (parsiDate.getDay() - 1)) % weekDaysCount);
    }

    // -------------------------

    public static String dayOfWeekName(JalaliDateTime parsiDate) {
        /*
         نام خورشیدی روز تاریخ مورد نظر
         "parsiDate"=>تاریخ خورشیدی

         نام خورشیدی روز تاریخ مورد نظر    
        */
        return parsiDayOfWeek.get(dayOfWeekNumber(parsiDate));
    }

    // ------------------

    public static int what5Or4(double LastDays) {
        /*
         محاسبه چندمین زیر دوره کوچک چهار ساله یا پنج ساله
         "LastDays"=>تعداد روز گذشته شده

         چندمین زیر دوره کوچک چهار ساله یا پنج ساله 
        */
        int What = 0;
        if (LastDays > daysCount5) { // 1826تعداد روز دوره کوچک 5 است
            LastDays -= daysCount5;
            What = (int) Math.ceil(LastDays / daysCount4);
        }

        return What;
    }

    // ----------------------------
    public static Object[] daysToYear(long LastDays, long RestDays) {
        /*
         تبدیل روز ها ی گذشته شده به سال پارسی
         "LastDays"=>تعداد روز گذشته شده
         "RestDays"=>روز باقی مانده

         تبدیل روز ها ی گذشته شده به سال پارسی 
        */
        long What2820 = which2820AtDay(LastDays); // کدام 2820 بزرگ
        long Years2820 = (What2820 - 1) * yearsCountBigAGE; // سال های 2820
        double RestDaysVal = (LastDays + daysCountBlinkYear) - ((What2820 - 1) * daysCountBigAGE);

        long What128Or132 = which128Or132AtDay(LastDays);

        long Years128Or132;
        if (What128Or132 > Count128Circuit) {
            Years128Or132 = yearsCount128AGES;
        } else {
            Years128Or132 = (What128Or132 - 1) * yearsCount128;
        }

        if (What128Or132 > Count128Circuit) {
            RestDaysVal -= (Count128Circuit * daysCount128);
        } else {
            RestDaysVal -= ((What128Or132 - 1) * daysCount128);
        }
        long Year = Years2820 + Years128Or132;
        long SubAge29Or33Or37 = whichSubAge29Or33Or37AtDay(LastDays);
        if ((SubAge29Or33Or37 >= 1) && (SubAge29Or33Or37 <= 3)) {
            RestDaysVal -= (daysCount29 + ((SubAge29Or33Or37 - 1) * daysCount33));
        } else {
            if (SubAge29Or33Or37 == yearsCount37) {
                RestDaysVal -= (daysCount29 + (2 * daysCount33));
            }
        }

        if ((SubAge29Or33Or37 >= 1) && (SubAge29Or33Or37 <= 3)) {
            Year += (yearsCount29 + ((SubAge29Or33Or37 - 1) * yearsCount33));
        } else {
            if (SubAge29Or33Or37 == yearsCount37) {
                Year += (yearsCount29 + (2 * yearsCount33));
            }
        }

        int SubAge5Or4 = what5Or4(RestDaysVal); // تعداد زیر دوره گذشته شده 5 یا 4 #تعداد سال کبیسه
        RestDaysVal -= ((arrYearNumber[SubAge5Or4] * daysCountNormalYear) + SubAge5Or4);
        double IN5O4 = (RestDaysVal - 1) / daysCountNormalYear;
        long Inside5Or4 = (long) Math.floor(IN5O4); // سال چندم زیر دوره 5 یا 4
        RestDaysVal -= (daysCountNormalYear * Inside5Or4); // داخل زیر دوره کوچک 5 یا 4
        if (((Inside5Or4 == yearsCount4) && (arrYearNumber[SubAge5Or4] > 0)) || (Inside5Or4 == yearsCount5)) { // چون یک سال کبیسه است و در ضزب محاسبه نمی شود
            RestDaysVal -= 1;
        }
        Year = (Year + arrYearNumber[SubAge5Or4] + Inside5Or4 + 1) - yearsCountblink; // با یک به خاطر اینکه از سال جدید آغاز شود چمع شد و از 2346 به این دلیل کم شد که سال های نادیده است
        return new Object[]{Year, (long) RestDaysVal};
    }

    // ------------------------------
    public static JalaliDateTime convertToParsiDate(LocalDateTime Date) {
        /*
         تبدیل تاریخ میلادی به خورشیدی
         "Date"=>تاریخ میلادی

         تاریخ خورشیدی 
         توضیح=>اگر تاریخ ورودی از تاریخ - 0622/03/22 - میلادی کمتر باشد خطا صادر می شود
        */
        LocalDateTime epoch = LocalDateTime.of(622, 3, 22, 0, 0, 0);
        if (Date.isBefore(epoch)) {
            throw new DAexception(".تاریخ 0622/03/22 میلادی برار با روز اول تاریخ خورشیدی است" + "\n .باید تاریخ ورودی برابر یا بزرگتر از این تاریخ باشد");
        }
        long RestDays = 0;
        long LastDays = ChronoUnit.DAYS.between(firstDay, Date); // تعداد روز گذشته شده از سال یک تا تاریخ ورودی #1/01/01=622/03/22
        Object[] FirstYearsAndLastDays = daysToYear(LastDays, RestDays); // بدست آوردن سال ابتدای سال تاریخ مورد نظر

        // parsiDate = DayToDate(FirstYears, RestDays)
        JalaliDateTime parsiDate = dayToDate((long) FirstYearsAndLastDays[0], (long) FirstYearsAndLastDays[1]);
        return new JalaliDateTime(parsiDate.getYear(), parsiDate.getMonth(), parsiDate.getDay(), Date.getHour(), Date.getMinute(), Date.getSecond(), (int) (Date.getNano() / 1000));
    }

    // ---------------------------------------

    public static JalaliDateTime now() {
        /*
         تاریخ خورشیدی
        */

        return convertToParsiDate(LocalDateTime.now());
    }

    public static LocalDateTime convertToGregorian(JalaliDateTime parsiDate) {
        /*
          تبدیل تاریخ خورشیدی به میلادی
          "parsiDate"=>تاریخ فارسی
          تبدیل تاریخ خورشیدی به میلادی    
        */
        long MultiplesAnomaly = multipleLeapYear(parsiDate.getYear()) - anomalyYearsCountBlink; // تعداد سال کبیسه کل منهای تعداد سال کبیسه نادیده
        long Days = ((parsiDate.getYear() - 1) * daysCountNormalYear) + MultiplesAnomaly; // تعداد کل روز های طی شده تا ابتدای سال مورد نظر

        long ExtraDays;
        if (parsiDate.getMonth() > monthCountEachYearMid) {
            ExtraDays = monthCountEachYearMid;
        } else {
            ExtraDays = (parsiDate.getMonth() - 1);
        }

        long OtherDays = ((parsiDate.getMonth() - 1) * daysCountYearSecMid) + ExtraDays; // ماه ها تا قبل از ماه مورد نظر ضرب در 30 و با روز اضافی مر بوط به فصل بهار و تابستان جمع شده
        Days += ((OtherDays + parsiDate.getDay()) - 1); // بدین دلیل از یک کم می شود چون 1/1/1 یک روز کامل است و در نظر گرفته شده است و می خواهیم از 0/0/0 حساب شود
        LocalDateTime date = firstDay.plusDays(Days);

        return LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), parsiDate.getHour(), parsiDate.getMinute(), parsiDate.getSecond(), parsiDate.getMicrosecond() * 1000);
    }

    public static JalaliDateTime strToParsiDate(String str) {
        /*
        تبدیل رشته به تاریخ خورشیدی
        */
        try {
            String[] dt = str.split(" ");

            String[] splitDate = dt[0].split("/");

            int y = Integer.parseInt(splitDate[0]);
            int m = Integer.parseInt(splitDate[1]);
            int d = Integer.parseInt(splitDate[2]);

            int hh = 0, mm = 0, ss = 0, micro = 0;

            if (dt.length > 1) {
                String[] splitTime = dt[1].split(":");

                hh = Integer.parseInt(splitTime[0]);
                mm = Integer.parseInt(splitTime[1]);

                if (splitTime.length > 2) {
                    String[] splitSM = splitTime[2].split("\\.");

                    ss = Integer.parseInt(splitSM[0]);
                    if (splitSM.length > 1) {
                        micro = Integer.parseInt(splitSM[1]);
                    }
                }
            }

            return new JalaliDateTime(y, m, d, hh, mm, ss, micro);

        } catch (Exception e) {
            throw new DAexception("Please enter correct date and time!?");
        }
    }
}