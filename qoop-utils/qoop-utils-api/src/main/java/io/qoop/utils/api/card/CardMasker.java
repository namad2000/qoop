package io.qoop.utils.api.card;

public final class CardMasker {

    private static final int VISIBLE_FIRST = 4;
    private static final int VISIBLE_LAST = 4;
    private static final int MIN_CARD_LENGTH = 12;
    private static final int MAX_CARD_LENGTH = 19;

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        String cleaned = cardNumber.replaceAll("\\D", "");

        if (cleaned.length() < MIN_CARD_LENGTH || cleaned.length() > MAX_CARD_LENGTH) {
            return cardNumber;
        }

        if (cleaned.length() <= (VISIBLE_FIRST + VISIBLE_LAST)) {
            return cleaned;
        }

        String firstPart = cleaned.substring(0, VISIBLE_FIRST);
        String lastPart = cleaned.substring(cleaned.length() - VISIBLE_LAST);
        int maskLength = cleaned.length() - (VISIBLE_FIRST + VISIBLE_LAST);
        String mask = "*".repeat(maskLength);

        return firstPart + mask + lastPart;
    }

    public static String maskCardNumberWithSpaces(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        String masked = maskCardNumber(cardNumber);

        if (masked == null) {
            return null;
        }

        // Formatting with spaces (every 4 digits)
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i += 4) {
            if (i > 0) {
                formatted.append(" ");
            }
            int end = Math.min(i + 4, masked.length());
            formatted.append(masked, i, end);
        }

        return formatted.toString();
    }
}
