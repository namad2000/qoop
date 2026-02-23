package io.qoop.utils.api.strings;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class StringUtil {

    private StringUtil() {
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String toEnglishNumbers(String persianNumberString) {
        if (persianNumberString == null || persianNumberString.isEmpty()) {
            return persianNumberString;
        }

        StringBuilder englishNumberBuilder = new StringBuilder();
        for (char c : persianNumberString.toCharArray()) {
            // Check if the character is a Persian digit (۰-۹)
            if (c >= '\u06F0' && c <= '\u06F9') {
                // Convert Persian digit to English digit
                englishNumberBuilder.append((char) (c - '\u06F0' + '0'));
            }
            // Also handle Arabic digits (٠-٩) which are sometimes used interchangeably
            else if (c >= '\u0660' && c <= '\u0669') {
                // Convert Arabic digit to English digit
                englishNumberBuilder.append((char) (c - '\u0660' + '0'));
            } else {
                // Append other characters as they are
                englishNumberBuilder.append(c);
            }
        }
        return englishNumberBuilder.toString();
    }
}
