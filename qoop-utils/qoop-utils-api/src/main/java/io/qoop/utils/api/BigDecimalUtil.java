package io.qoop.utils.api;


import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BigDecimalUtil {

    private BigDecimalUtil() {
    }

    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    /* =========================
       Null Safe
     ========================= */

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    /* =========================
       Compare
     ========================= */

    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).compareTo(zeroIfNull(b)) > 0;
    }

    public static boolean lessThan(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).compareTo(zeroIfNull(b)) < 0;
    }

    public static boolean equals(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).compareTo(zeroIfNull(b)) == 0;
    }

    /* =========================
       Math Operations
     ========================= */

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).add(zeroIfNull(b));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).subtract(zeroIfNull(b));
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).multiply(zeroIfNull(b));
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b, int scale) {
        return multiply(a, b).setScale(scale, DEFAULT_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (isNullOrZero(b)) {
            throw new ArithmeticException("Division by zero");
        }
        return zeroIfNull(a).divide(b, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    /* =========================
       Price & Discount
     ========================= */

    public static BigDecimal percent(BigDecimal value, BigDecimal percent) {
        return multiply(value, percent)
                .divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal applyDiscountPercent(BigDecimal price, BigDecimal discountPercent) {
        if (isNullOrZero(price) || isNullOrZero(discountPercent)) {
            return zeroIfNull(price);
        }
        return subtract(price, percent(price, discountPercent));
    }

    public static BigDecimal applyDiscountAmount(BigDecimal price, BigDecimal discountAmount) {
        return subtract(price, discountAmount).max(BigDecimal.ZERO);
    }

    /* =========================
       Scale & Rounding
     ========================= */

    public static BigDecimal scale(BigDecimal value) {
        return zeroIfNull(value).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal scale(BigDecimal value, int scale) {
        return zeroIfNull(value).setScale(scale, DEFAULT_ROUNDING);
    }

    /* =========================
       Min / Max
     ========================= */

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).min(zeroIfNull(b));
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return zeroIfNull(a).max(zeroIfNull(b));
    }
}

